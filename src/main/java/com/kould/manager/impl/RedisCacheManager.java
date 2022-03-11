package com.kould.manager.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.kould.config.KacheAutoConfig;
import com.kould.enity.NullValue;
import com.kould.lock.KacheLock;
import com.kould.manager.RemoteCacheManager;
import com.kould.service.RedisService;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.Lock;

/*
使用Redis进行缓存存取的CacheManager
通过Lua脚本进行主要的存取实现
实现散列化的形式进行缓存存储
 */
public class RedisCacheManager extends RemoteCacheManager {

    private static final RedisCacheManager INSTANCE = new RedisCacheManager() ;

    private static final Logger log = LoggerFactory.getLogger(RedisCacheManager.class) ;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KacheLock kacheLock ;

    private static final NullValue NULL_VALUE = new NullValue();

    private static final Object COLLECTION_KRYO = new ArrayList<>();

    private static final String METHOD_GET_ID = "getId" ;

    //Lua脚本，用于在Redis中通过Redis中的索引收集获取对应的散列PO类
    private static final  String SCRIPT_LUA_CACHE_GET =
                    "local keys = redis.call('lrange',KEYS[1],0,redis.call('llen',KEYS[1])) " +
                    "local keySize = table.getn(keys) " +
                    "if(next(keys) == nil) " +
                    "then " +
                    "   return nil " +
                    "elseif(keySize == 1) " +
                     "then " +
                    "   return keys " +
                    "end " +
                    "local keyFirst = keys[1] " +
                    "table.remove(keys,1) " +
                    "local result = redis.call('mget',unpack(keys)) " +
                    "table.insert(result,1,keyFirst) " +
                    "if(keySize == table.getn(result)) " +
                    "then " +
                    "   return result " +
                    "else " +
                    "   return nil " +
                    "end " ;

    //Lua脚本，用于在Reids中获取符合表达式的索引
    private static final  String SCRIPT_LUA_CACHE_KEYS =
                    "local cursor = 0 " +
                    "local resp = redis.call('SCAN',cursor,'MATCH',KEYS[1],'COUNT',10) " +
                    "cursor = tonumber(resp[1]) " +
                    "local result = resp[2] " +
                    "while(cursor ~= 0) do " +
                    "    local resp1 = redis.call('SCAN',cursor,'MATCH',KEYS[1],'COUNT',10) " +
                    "    cursor = tonumber(resp1[1]) " +
                    "       for key,value in pairs(resp1[2]) do " +
                    "           table.insert(result,value) " +
                    "       end " +
                    "end " +
                    "return result " ;

    private String scriptGetSHA1 ;

    private String scriptKeysSHA1 ;

    private static final Object NULL_TAG_VALUE = new NullValue();

    private RedisCacheManager() {}

    public static RedisCacheManager getInstance() {
        return INSTANCE ;
    }

    /**
     * 初始化预先缓存对应Lua脚本并取出脚本SHA1码存入变量
     */
    @PostConstruct
    private void init() throws Throwable {
        redisService.executeSync(commands -> {
            scriptGetSHA1 = commands.scriptLoad(SCRIPT_LUA_CACHE_GET);
            scriptKeysSHA1 = commands.scriptLoad(SCRIPT_LUA_CACHE_KEYS);
            return true;
        });
    }

    @Override
    public String getNullTag() {
        return "To The Moon";
    }

    /**
     * 以拼接lua语句脚本的形式多次提交，规则为：
     *  索引：以list的形式存入，以result的类型做多种处理
     *      1、若result为Collection或包装类：
     *          （若为包装类会提前提出其中的保存PO数据的属性与Collection同样处理）
     *          将数据逐条获取id当作key，数据为value，以此对齐分别存入id集合和具体数据集合中
     *          （keys和values则为Lua脚本对参数的包装）；
     *          并将首条数据将Collection或包装类除去真实数据保存其状态；
     *          最后将参数key作为key和上述的id集作为value组成一条list存入，而每条id则分别与对应的PO进行存入
     *      2、若result为一条POBean：
     *          则将直接获取result的id并与result作为单条PO存入；
     *          再将key和id作为键值对，存入作为索引
     * @param key 索引值
     * @param lockKey 锁值
     * @param point 切入点
     * @return 存入成功返回传入的result，失败则返回null
     */
    @Override
    public Object put(String key, String lockKey, ProceedingJoinPoint point) throws Throwable {
        return redisService.executeSync(commands -> {
            Lock writeLock = null;
            try {
                StringBuilder lua = new StringBuilder();
                List<String> keys = new ArrayList<>() ;
                List<Object> values = new ArrayList<>() ;
                writeLock = kacheLock.writeLock(lockKey);
                Object result = point.proceed();
                if (result instanceof Collection) {
                    collection2Lua(commands, key, lua, keys, values, (Collection) result,null);
                } else if (result != null && isHasField(result.getClass(), dataFieldProperties.getName())) {
                    Field recordsField = result.getClass().getDeclaredField(dataFieldProperties.getName());
                    recordsField.setAccessible(true);
                    Collection<Object> records = (Collection) recordsField.get(result) ;
                    recordsField.set(result,Collections.emptyList());
                    collection2Lua(commands, key, lua, keys, values, records, result);
                    recordsField.set(result,records);
                } else {
                    lua.append("redis.call('setex',KEYS[1],")
                            .append(strategyHandler.getCacheTime())
                            .append(",ARGV[1]);");
                    String id = null ;
                    if (!key.contains(KacheAutoConfig.NO_ID_TAG)) {
                        //若为ID方法，则直接将key赋值给id
                        id = key ;
                    } else if (result != null) {
                        //获取条件方法单结果
                        Method methodGetId = result.getClass().getMethod(METHOD_GET_ID, null);
                        id = KacheAutoConfig.CACHE_PREFIX + methodGetId.invoke(result).toString() ;
                    } else {
                        id = KacheAutoConfig.CACHE_PREFIX + getNullTag() ;
                    }
                    if (result == null) {
                        values.add(NULL_VALUE) ;
                    } else {
                        values.add(result) ;
                    }
                    keys.add(id) ;
                    //判断此时是否为id获取的单结果或者为条件查询获取的单结果
                    if (!key.equals(id)) {
                        keys.add(key) ;
                        values.add(id) ;
                        lua.append("redis.call('del',KEYS[2]);");
                        lua.append("redis.call('lpush',KEYS[2],ARGV[2]);") ;
                        lua.append("return redis.call('expire',KEYS[2],")
                                .append(strategyHandler.getCacheTime())
                                .append(");");
                    }
                }
                commands.eval(lua.toString(), ScriptOutputType.MULTI
                        , keys.toArray(new String[keys.size()]), values.toArray(new Object[values.size()])) ;
                kacheLock.unLock(writeLock);
                return result ;
            } catch (Exception e) {
                if (writeLock != null && kacheLock.isLockedByThisThread(writeLock)) {
                    kacheLock.unLock(writeLock);
                }
                throw e ;
            }
        });
    }

    private void collection2Lua(RedisCommands<String, Object> commands, String key, StringBuilder lua, List<String> keys, List<Object> values, Collection<Object> records , Object page) throws Exception {
        StringBuilder idsNum = new StringBuilder();
        StringBuilder echo = new StringBuilder() ;
        //用于收集哪些元数据是否已存在（每个元素指的是records中的元素索引）
        List<Long> echoIds ;
        Integer count = 0;
        int delCount = 0 ;
        Iterator<Object> iterator = records.iterator();
        if (iterator.hasNext()) {
            //生成Echo脚本（返回Redis内不存在的单条数据，用于减少重复的数据，减少重复序列化和多余的IO占用）
            Method methodGetId = records.iterator().next().getClass().getMethod(METHOD_GET_ID,null) ;
            int count2Echo = 0 ;
            echo.append("local result = {} ") ;
            while(iterator.hasNext()) {
                count2Echo ++ ;
                keys.add(KacheAutoConfig.CACHE_PREFIX + methodGetId.invoke(iterator.next()).toString());
                echo.append("if(redis.call('EXISTS',KEYS[")
                        .append(count2Echo)
                        .append("]) == 0) ")
                        .append("then ")
                        //若不存在即加入result中被返回
                        .append("table.insert(result,")
                        .append(count2Echo)
                        .append(") ")
                        .append("else ")
                        //若存在则延长命中缓存的存活时间
                        .append("redis.call('expire',KEYS[").append(count2Echo).append("],")
                        .append(strategyHandler.getCacheTime()).append("); ")
                        .append("end ") ;
            }
            echo.append("return result ") ;
            echoIds = commands.eval(echo.toString(), ScriptOutputType.MULTI
                    , keys.toArray(new String[keys.size()]));
            int used = 0;
            for (Object record : records) {
                count ++ ;
                if (echoIds.size() > used && echoIds.get(used) == count.longValue()) {
                    used ++;
                    //单条数据缓存
                    lua.append("redis.call('setex',KEYS[")
                            .append(count)
                            .append("],")
                            .append(strategyHandler.getCacheTime())
                            .append(",ARGV[")
                            .append(count - delCount)
                            .append("]);");
                    values.add(record);
                } else {
                    delCount ++ ;
                }
                //拼接id聚合参数
                idsNum.append(",ARGV[")
                        .append(count + echoIds.size())
                        .append("]");
            }
            idsNum.append(",ARGV[")
                    .append(++count + echoIds.size())
                    .append("]");
            //聚合缓存
            lua.append("redis.call('del',KEYS[")
                    .append(records.size() + 1)
                    .append("]);");
            lua.append("redis.call('lpush',KEYS[")
                    .append(records.size() + 1)
                    .append("]")
                    .append(idsNum)
                    .append(");");
            lua.append("return redis.call('expire',KEYS[")
                    .append(records.size() + 1)
                    .append("],")
                    .append(strategyHandler.getCacheTime())
                    .append(");");
            values.addAll(keys);
            if (page != null) {
                values.add(page) ;
            } else {
                values.add(COLLECTION_KRYO) ;
            }
            keys.add(key);
        } else {
            lua.append("redis.call('del',KEYS[1]) ");
            lua.append("redis.call('lpush',KEYS[1],ARGV[1]) ") ;
            lua.append("return redis.call('expire',KEYS[1],")
                    .append(strategyHandler.getCacheTime())
                    .append(");");
            keys.add(key) ;
            values.add(page) ;
        }
    }

    /**
     * 使用Lua脚本在Redis处直接完成索引值的散列数据收集并以List的形式返回
     * 随后解析list：
     *  若list的首位数据为"[]"或"{}"则说明为Collection（目前还只是支持List）
     *      则直接生成一个ArrayList records并遍历list首位的以后的数据并反序列化加入到records中
     *   若List的大小为1，则说明为单条PO
     *      则直接反序列化为Bean并返回
     *   若以上条件都不满足则说明为包装类
     *      应首条数据的反序列化，得到其状态后再如同Collection的处理
     *      填充其中配置文件所设置的对应数据集属性
     * @param key 索引值
     * @return 成功返回对应Key的结果，失败则返回null
     */
    @Override
    public Object get(String key, String lockKey) throws Throwable {
        return redisService.executeSync(commands -> {
            Lock readLock = null ;
            try {
                //判断是否为直接通过ID获取单条方法
                if (!key.contains(KacheAutoConfig.NO_ID_TAG)) {
                    readLock = kacheLock.readLock(lockKey);
                    Object result = commands.get(key);
                    kacheLock.unLock(readLock);
                    return result;
                } else {
                    //为条件查询方法
                    readLock = kacheLock.readLock(lockKey);
                    List<Object> list = commands.evalsha(scriptGetSHA1, ScriptOutputType.MULTI, key);
                    kacheLock.unLock(readLock);
                    List<Object> records = new ArrayList<>();
                    if (list != null && !list.isEmpty()) {
                        Object first = list.get(0);
                        //判断结果是否为单个POBean
                        if (list.size() == 1) {
                            return list.get(0);
                            //判断返回结果是否为Collection或其子类
                        } else if (first instanceof Collection) {
                            //跳过第一位数据的填充
                            for (int i = 1; i < list.size(); i++) {
                                records.add(list.get(i));
                            }
                            //由于Redis内list的存储是类似栈帧压入的形式导致list存取时倒叙，所以此处取出时将顺序颠倒回原位
                            Collections.reverse(records);
                            return records;
                        } else {
                            //此时为包装类的情况
                            Field recordsField = first.getClass().getDeclaredField(dataFieldProperties.getName());
                            recordsField.setAccessible(true);
                            recordsField.set(first, records);
                            //跳过第一位数据的填充
                            for (int i = 1; i < list.size(); i++) {
                                records.add(list.get(i));
                            }
                            //由于Redis内list的存储是类似栈帧压入的形式导致list存取时倒叙，所以此处取出时将顺序颠倒回原位
                            Collections.reverse(records);
                            return first;
                        }
                    } else return null;
                }
            } catch (Exception e) {
                if (readLock != null && kacheLock.isLockedByThisThread(readLock)) {
                    kacheLock.unLock(readLock);
                }
                throw e ;
            }
        }) ;
    }

    @Override
    public Object unLockGet(String key) throws Throwable {
        return redisService.executeSync(commands -> commands.get(key));
    }

    @Override
    public Object unLockPut(String key, ProceedingJoinPoint point) throws Throwable {
        return redisService.executeSync(commands -> {
            Object proceed = point.proceed();
            return commands.set(key,proceed);
        });
    }

    @Override
    public List<String> keys(String pattern) throws Throwable {
        return redisService.executeSync(
                commands -> commands.evalsha(scriptKeysSHA1, ScriptOutputType.MULTI, pattern));
    }

    @Override
    public Long del(String... keys) throws Throwable {
        return redisService.executeSync(commands -> commands.del(keys));
    }

    @Override
    public Object updateById(String id, Object result) throws Throwable {
       return redisService.executeSync(commands -> {
           String key = KacheAutoConfig.CACHE_PREFIX + id ;
           Object target = commands.get(key);
           if (target != null) {
               BeanUtil.copyProperties(result, target,
                       true, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
               commands.setex(key, strategyHandler.getCacheTime(), target);
               return target;
           } else {
               return null ;
           }
       });
    }

    /**
     * 通过获取该Class的声明对象是否能够成功来判断其是否存在该属性
     * @param clazz 目标Class
     * @param field 属性名
     * @return 存在返回true 不存在返回false
     */
    private boolean isHasField(Class<?> clazz, String field) {
        try {
            clazz.getDeclaredField(field);
            return true ;
        } catch (NoSuchFieldException e) {
            return false ;
        }
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
