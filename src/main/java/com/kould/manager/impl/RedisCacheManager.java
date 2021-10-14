package com.kould.manager.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.kould.bean.KacheConfig;
import com.kould.json.JsonUtil;
import com.kould.manager.RemoteCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RedisCacheManager implements RemoteCacheManager {

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private JsonUtil jsonUtil ;

    @Autowired
    private KacheConfig kacheConfig ;

    private static final String METHOD_GET_ID = "getId" ;

    //用于标记空值的Tag，值为最喜欢的游戏之一“To the moon”
    private static final String NULL_TAG = "To The Moon" ;

    //静态Lua脚本，用于在Redis中通过Redis中的索引收集获取对应的散列PO类
    private static final  String SCRIPT_LUA_CACHE_GET =
                    "local keys = redis.call('lrange',KEYS[1],0,redis.call('llen',KEYS[1])) "+
                    "local result = {} " +
                    "for key,value in pairs(keys) do " +
                    "    local data = redis.call('get',value) " +
                    "    if(data) " +
                    "    then " +
                    "        table.insert(result,data) " +
                    "    else" +
                    //若get不到值则说明为包装类的序列化，所有默认当作第一位，方便后继处理
                    "        if(key == 1) " +
                            "then " +
                                "table.insert(result,1,value) " +
                            "else " +
                                "return nil " +
                            "end " +
                    "    end " +
                    "end " +
                    "return result " ;

    //静态Lua脚步，用于在Reids中获取符合表达式的索引
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

    private void close(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    @Override
    public boolean hasKey(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(key) ;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close(jedis);
        }
        return false ;
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
     * @param result 返回结果
     * @param <T>
     * @return 存入成功返回传入的result，失败则返回null
     */
    @Override
    public <T> T put(String key, T result) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            StringBuilder lua = new StringBuilder();
            List<String> keys = new ArrayList<>() ;
            List<String> values = new ArrayList<>() ;
            if (result instanceof Collection) {
                collection2Lua(key, lua, keys, values, (Collection) result,null);
            } else if (isHasField(result.getClass(), kacheConfig.getDataFieldName())) {
                Field recordsField = result.getClass().getDeclaredField(kacheConfig.getDataFieldName());
                recordsField.setAccessible(true);
                Collection records = (Collection) recordsField.get(result) ;
                recordsField.set(result,null);
                collection2Lua(key, lua, keys, values, records, result);
                recordsField.set(result,records);
            } else {
                lua.append("redis.call('setex',KEYS[1],")
                        .append(kacheConfig.getCacheTime())
                        .append(",ARGV[1]);");
                lua.append("redis.call('del',KEYS[2]);");
                lua.append("redis.call('lpush',KEYS[2],ARGV[2]);") ;
                lua.append("return redis.call('expire',KEYS[2],")
                        .append(kacheConfig.getCacheTime())
                        .append(");");
                String id = null ;
                if (result != null) {
                    Method methodGetId = result.getClass().getMethod(METHOD_GET_ID, null);
                    id = methodGetId.invoke(result).toString() ;
                } else {
                    id = NULL_TAG ;
                }
                keys.add(id) ;
                keys.add(key) ;
                values.add(jsonUtil.obj2Str(result)) ;
                values.add(keys.get(0)) ;
            }
            jedis.eval(lua.toString(),keys,values) ;
            return result ;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close(jedis);
        }
        return null ;
    }

    private void collection2Lua(String key, StringBuilder lua, List<String> keys, List<String> values, Collection<Object> records ,Object page) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        StringBuilder idsNum = new StringBuilder();
        int count = 0;
        Method methodGetId = records.iterator().next()
                .getClass().getMethod(METHOD_GET_ID, null);
        for (Object record : records) {
            //单条数据缓存
            lua.append("redis.call('setex',KEYS[")
                    .append(++count)
                    .append("],")
                    .append(kacheConfig.getCacheTime())
                    .append(",ARGV[")
                    .append(count)
                    .append("]);");
            keys.add(methodGetId.invoke(record).toString());
            values.add(jsonUtil.obj2Str(record));
            //拼接id聚合参数
            idsNum.append(",ARGV[")
                    .append(count + records.size())
                    .append("]");
        }
        idsNum.append(",ARGV[")
                .append(++count + records.size())
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
                .append(kacheConfig.getCacheTime())
                .append(");");
        values.addAll(keys);
        if (page != null) {
            values.add(jsonUtil.obj2Str(page));
        } else {
            values.add("[]") ;
        }
        keys.add(key);
    }

    /**
     * 使用Lua脚本在Redis处直接完成索引值的散列数据收集并以List的形式返回
     * 随后解析list：
     *  若list的首位数据为"[]"或"{}"则说明为Collection（目前还只是支持List）
     *      则直接生成一个ArrayList records并遍历list首位的以后的数据并反序列化加入到records中
     *   若List的大小为1，则说明为单条PO
     *      则直接反序列化为Bean并返回
     *   若以上条件都不满足则说明为包装类
     *      通过参数中的resultClass进行对应首条数据的反序列化，得到其状态后再如同第一条
     *      填充其中配置文件所设置的对应数据集属性
     * @param key 索引值
     * @param resultClass 返回包装类型
     * @param beanClass PO类型
     * @return 成功返回对应Key的结果，失败则返回null
     */
    @Override
    public Object get(String key, Class<?> resultClass, Class<?> beanClass) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            List<String> list = (ArrayList)jedis.eval(SCRIPT_LUA_CACHE_GET, 1, key );
            List<Object> records = new ArrayList() ;
            if ( list != null && !list.isEmpty()) {
                if (list.get(0).equals("[]") && list.get(0).equals("{}")) {
                    List<Object> result = new ArrayList();
                    //跳过第一位数据的填充
                    for (int i = 1; i < list.size(); i++) {
                        records.add(jsonUtil.str2Obj(list.get(i), beanClass));
                    }
                    //由于Redis内list的存储是类似栈帧压入的形式导致list存取时倒叙，所以此处取出时将顺序颠倒回原位
                    Collections.reverse(records);
                    return result;
                } else if(list.size() == 1) {
                    return jsonUtil.str2Obj(list.get(0), beanClass);
                } else {
                    Object result = jsonUtil.str2Obj(list.get(0), resultClass);
                    Field recordsField = result.getClass().getDeclaredField(kacheConfig.getDataFieldName());
                    recordsField.setAccessible(true);
                    recordsField.set(result,records);
                    //跳过第一位数据的填充
                    for (int i = 1; i < list.size(); i++) {
                        records.add(jsonUtil.str2Obj(list.get(i),beanClass)) ;
                    }
                    //由于Redis内list的存储是类似栈帧压入的形式导致list存取时倒叙，所以此处取出时将顺序颠倒回原位
                    Collections.reverse(records);
                    return result ;
                }
            } else return null ;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close(jedis);
        }
        return null ;
    }

    @Override
    public List<String> keys(String pattern) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return (ArrayList)jedis.eval(SCRIPT_LUA_CACHE_KEYS, 1, pattern);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close(jedis);
        }
        return null ;
    }

    @Override
    public Long del(String... keys) {
        Jedis jedis = null;
        try {
            if (keys != null) {
                jedis = jedisPool.getResource();
                return jedis.del(keys);
            }
            return 0L;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close(jedis);
        }
        return 0L;
    }

    @Override
    public <T> T updateById(String id, T result) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String s = jedis.get(id);
            if (s != null) {
                Object targer = jsonUtil.str2Obj(s, result.getClass());
                BeanUtil.copyProperties(result, targer,
                        true, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
                jedis.setex(id, kacheConfig.getCacheTime(), jsonUtil.obj2Str(targer));
                return (T) targer;
            } else {
                return null ;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close(jedis);
        }
        return null ;
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
}
