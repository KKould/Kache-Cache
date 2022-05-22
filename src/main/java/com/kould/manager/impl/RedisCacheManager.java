package com.kould.manager.impl;

import com.kould.properties.DaoProperties;
import com.kould.properties.DataFieldProperties;
import com.kould.api.Kache;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.NullValue;
import com.kould.manager.RemoteCacheManager;
import com.kould.entity.MethodPoint;
import com.kould.service.RedisService;
import com.kould.utils.FieldUtils;
import io.lettuce.core.*;
import io.lettuce.core.api.sync.RedisCommands;

import java.lang.reflect.Field;
import java.util.*;

/*
使用Redis进行缓存存取的CacheManager
通过Lua脚本进行主要的存取实现
实现散列化的形式进行缓存存储
 */
public class RedisCacheManager extends RemoteCacheManager {

    private static final String INDEX_TAG_KEY = Kache.CACHE_PREFIX + Kache.INDEX_TAG;

    private static final Object COLLECTION_KRYO = new ArrayList<>();

    //Lua脚本，用于在Redis中通过Redis中的索引收集获取对应的散列PO类
    //需要优化Kache.SERVICE_BY_FIELD的查找，使用索引优化
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
                    "local container = keys[1] " +
                    "table.remove(keys,1) " +
                    "local result = redis.call('mget',unpack(keys)) " +
                    "table.insert(result,1,container) " +
                    // 此处可能会因为增删改导致元缓存被删除
                    // 若元缓存与索引id集内数量不匹配则返回Null并从数据库获取最新数据
                    "if(keySize == table.getn(result)) " +
                    "then " +
                    "   return result " +
                    "else " +
                    "   return nil " +
                    "end " ;

    private String scriptGetSHA1 ;

    private final RedisService redisService;

    private final CacheEncoder cacheEncoder;

    public RedisCacheManager(DataFieldProperties dataFieldProperties, DaoProperties daoProperties, RedisService redisService, CacheEncoder cacheEncoder) {
        super(dataFieldProperties, daoProperties);
        this.redisService = redisService;
        this.cacheEncoder = cacheEncoder;
    }


    /**
     * 初始化预先缓存对应Lua脚本并取出脚本SHA1码存入变量
     */
    @Override
    public void init() throws Exception {
        redisService.executeSync(commands -> {
            scriptGetSHA1 = commands.scriptLoad(SCRIPT_LUA_CACHE_GET);
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
     * @param type 主元缓存类型名
     * @param point 切入点
     * @return 存入成功返回传入的result，失败则返回null
     */
    @Override
    public Object put(String key, String type, MethodPoint point) throws Exception {
        return redisService.executeSync(commands -> {
            String recordsName = dataFieldProperties.getRecordsName();
            Object result = point.execute();
            if (result instanceof Collection) {
                collection2Lua(commands, key, type, (Collection<Object>) result,null);
            } else if (result != null && isHasField(result.getClass(), recordsName)) {
                Field recordsField = FieldUtils.getFieldByNameAndClass(result.getClass(), recordsName);
                Collection<Object> records = (Collection<Object>) recordsField.get(result) ;
                recordsField.set(result,Collections.emptyList());
                collection2Lua(commands, key, type, records, result);
                recordsField.set(result,records);
            } else {
                String[] keys = new String[2];
                Object[] values = new Object[2];
                StringBuilder lua = new StringBuilder();
                lua.append("redis.call('setex',KEYS[1],")
                        .append(daoProperties.getCacheTime())
                        .append(",ARGV[1]);");
                if (!key.contains(INDEX_TAG_KEY)) {
                    //若为ID方法，则直接将key赋值给id
                    keys[0] = cacheEncoder.getId2Key(key, type);
                } else if (result != null) {
                    //获取条件方法单结果
                    Field primaryKeyField = FieldUtils.getFieldByNameAndClass(result.getClass()
                            , dataFieldProperties.getPrimaryKeyName());
                    keys[0] = cacheEncoder.getId2Key((String) primaryKeyField.get(result), type);
                } else {
                    //通过将类型设为null使NullTag被通用
                    keys[0] = cacheEncoder.getId2Key(getNullTag(), null);
                }
                if (result == null) {
                    values[0] = NullValue.getInstance();
                } else {
                    values[0] = result;
                }
                //判断此时是否为id获取的单结果或者为条件查询获取的单结果
                if (!key.equals(keys[0])) {
                    keys[1] = key;
                    values[1] = keys[0];
                    lua.append("redis.call('del',KEYS[2]);");
                    lua.append("redis.call('lpush',KEYS[2],ARGV[2]);") ;
                    lua.append("return redis.call('expire',KEYS[2],")
                            .append(daoProperties.getCacheTime())
                            .append(");");
                }
                commands.eval(lua.toString(), ScriptOutputType.MULTI, keys, values) ;
            }
            return result ;
        });
    }

    private void collection2Lua(RedisCommands<String, Object> commands, String key, String type
            , Collection<Object> records , Object page) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder idsNum = new StringBuilder();
        StringBuilder echo = new StringBuilder() ;
        StringBuilder lua = new StringBuilder();
        //用于收集哪些元数据是否已存在（每个元素指的是records中的元素索引）
        List<Long> echoIds ;
        Integer count = 0;
        int delCount = 0;
        Iterator<Object> iterator = records.iterator();
        String[] keys = new String[records.size() + 1];
        if (iterator.hasNext()) {
            //生成Echo脚本（返回Redis内不存在的单条数据，用于减少重复的数据，减少重复序列化和多余的IO占用）
            int count2Echo = 0 ;
            echo.append("local result = {} ") ;
            while(iterator.hasNext()) {
                Object next = iterator.next();
                count2Echo ++ ;
                keys[count2Echo - 1] = cacheEncoder.getId2Key(FieldUtils.getFieldByNameAndClass(next.getClass(), dataFieldProperties.getPrimaryKeyName())
                        .get(next).toString(), type);
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
                        .append(daoProperties.getCacheTime()).append("); ")
                        .append("end ") ;
            }
            echo.append("return result ") ;
            String[] echoKeys = new String[records.size()];
            System.arraycopy(keys,0,echoKeys,0,records.size());
            echoIds = commands.eval(echo.toString(), ScriptOutputType.MULTI
                    , echoKeys);
            int used = 0;
            int valuesSize = echoIds.size() + keys.length;
            Object[] values = new Object[valuesSize];
            for (Object next : records) {
                count++;
                //保证echoIds的总数大于于used，并筛选其中Redis没有的数据加以存储到Redis之中
                if (echoIds.size() > used && echoIds.get(used) == count.longValue()) {
                    //单条数据缓存
                    lua.append("redis.call('setex',KEYS[")
                            .append(count)
                            .append("],")
                            .append(daoProperties.getCacheTime())
                            .append(",ARGV[")
                            .append(count - delCount)
                            .append("]);");
                    values[used] = next;
                    used++;
                } else {
                    delCount++;
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
                    .append(daoProperties.getCacheTime())
                    .append(");");
            if (keys.length - 1 >= 0) {
                System.arraycopy(keys, 0, values, used, keys.length - 1);
            }
            if (page != null) {
                values[valuesSize - 1] = page;
            } else {
                values[valuesSize - 1] = COLLECTION_KRYO;
            }
            keys[keys.length - 1] = key;
            commands.eval(lua.toString(), ScriptOutputType.MULTI, keys, values);
        } else {
            lua.append("redis.call('del',KEYS[1]) ");
            lua.append("redis.call('lpush',KEYS[1],ARGV[1]) ") ;
            lua.append("return redis.call('expire',KEYS[1],")
                    .append(daoProperties.getCacheTime())
                    .append(");");
            keys[0] = key;
            commands.eval(lua.toString(), ScriptOutputType.MULTI, keys, page);
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
    public Object get(String key) throws Exception {
        return redisService.executeSync(commands -> {
            //判断是否为直接通过ID获取单条方法
            if (!key.contains(INDEX_TAG_KEY)) {
                return commands.get(key);
            } else {
                //为条件查询方法
                List<Object> list = commands.evalsha(scriptGetSHA1, ScriptOutputType.MULTI, key);
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
                        FieldUtils.getFieldByNameAndClass(first.getClass(), dataFieldProperties.getRecordsName())
                                .set(first, records);
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
        }) ;
    }

    @Override
    public Long delKeys(String pattern) throws Exception {
        return redisService.executeSync(commands -> {
            // SCAN参数
            ScanArgs scanArgs = ScanArgs.Builder.limit(500).match(pattern);
            // TEMP游标
            ScanCursor cursor = ScanCursor.INITIAL;
            long counter = 0L;
            do {
                KeyScanCursor<String> result = commands.scan(cursor, scanArgs);
                // 重置TEMP游标
                cursor = ScanCursor.of(result.getCursor());
                cursor.setFinished(result.isFinished());
                List<String> values = result.getKeys();
                if (!values.isEmpty()) {
                    commands.del(values.toArray(new String[values.size()]));
                }
                counter++;
            } while (!(ScanCursor.FINISHED.getCursor().equals(cursor.getCursor()) && ScanCursor.FINISHED.isFinished() == cursor.isFinished()));
            return counter;
        });
    }

    @Override
    public Long del(String... keys) throws Exception {
        return redisService.executeSync(commands -> commands.del(keys));
    }

    /**
     * 通过获取该Class的声明对象是否能够成功来判断其是否存在该属性
     * @param clazz 目标Class
     * @param field 属性名
     * @return 存在返回true 不存在返回false
     */
    private boolean isHasField(Class<?> clazz, String field) {
        try {
            FieldUtils.getFieldByNameAndClass(clazz, field);
            return true;
        } catch (NoSuchFieldException e) {
            return false ;
        }
    }
}
