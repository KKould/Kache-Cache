package com.kould.manager.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.reflect.TypeToken;
import com.kould.bean.KacheConfig;
import com.kould.json.JsonUtil;
import com.kould.manager.RemoteCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Method;
import java.util.*;

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

    private static final String NULL_TAG = "To The Moon" ;

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

    @Override
    public <T> T put(String key, T result) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            StringBuilder lua = new StringBuilder();
            List<String> keys = new ArrayList<>() ;
            List<String> values = new ArrayList<>() ;
            if (result instanceof IPage) {
                //分页包装类处理
                IPage<Object> page = new Page() ;
                BeanUtils.copyProperties(result, page);
                //包装类与数据List分离
                List records = page.getRecords();
                page.setRecords(null) ;
                //通过id收集来进行数据的关联
                StringBuilder idsNum = new StringBuilder() ;
                int count = 0 ;
                for (Object record : records) {
                    //单条数据缓存
                    lua.append("redis.call('setex',KEYS["+ ++count +"],"+ kacheConfig.getCacheTime() +",ARGV[" + count + "]);");
                    Method methodGetId = record.getClass().getMethod(METHOD_GET_ID, null);
                    keys.add(methodGetId.invoke(record).toString()) ;
                    values.add(jsonUtil.obj2Str(record)) ;
                    //拼接id聚合参数
                    idsNum.append(",ARGV[" + (count + records.size()) + "]") ;
                }
                idsNum.append(",ARGV[" + (++count + records.size()) + "]") ;
                //聚合缓存
                lua.append("redis.call('del',KEYS[" + (records.size() + 1) + "]);");
                lua.append("redis.call('lpush',KEYS[" + (records.size() + 1) + "]" + idsNum + ");");
                lua.append("return redis.call('expire',KEYS[" + (records.size() + 1) + "]," + kacheConfig.getCacheTime() + ");");
                values.addAll(keys) ;
                values.add(jsonUtil.obj2Str(page)) ;
                keys.add(key) ;
            } else if (result instanceof List) {
                List results = (List) result ;
                List list = results.getClass().newInstance();
                //包装类与数据List分离
                List<Object> records = new ArrayList<>();
                records.addAll(results);
                StringBuilder idsNum = new StringBuilder() ;
                int count = 0 ;
                for (Object record : records) {
                    //单条数据缓存
                    lua.append("redis.call('setex',KEYS["+ ++count +"],"+ kacheConfig.getCacheTime() +",ARGV[" + count + "]);");
                    Method methodGetId = record.getClass().getMethod(METHOD_GET_ID, null);
                    keys.add(methodGetId.invoke(record).toString()) ;
                    values.add(jsonUtil.obj2Str(record)) ;
                    //拼接id聚合参数
                    idsNum.append(",ARGV[" + (count + records.size()) + "]") ;
                }
                idsNum.append(",ARGV[" + (++count + records.size()) + "]") ;
                //聚合缓存
                lua.append("redis.call('del',KEYS[" + (records.size() + 1) + "]);");
                lua.append("redis.call('lpush',KEYS[" + (records.size() + 1) + "]" + idsNum + ");");
                lua.append("return redis.call('expire',KEYS[" + (records.size() + 1) + "]," + kacheConfig.getCacheTime() + ");");
                values.addAll(keys) ;
                values.add(jsonUtil.obj2Str(list)) ;
                keys.add(key) ;
            } else {
                lua.append("redis.call('setex',KEYS[1],"+ kacheConfig.getCacheTime() +",ARGV[1]);");
                lua.append("redis.call('del',KEYS[2]);");
                lua.append("redis.call('lpush',KEYS[2],ARGV[2]);") ;
                lua.append("return redis.call('expire',KEYS[2]," + kacheConfig.getCacheTime() + ");") ;
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

    @Override
    public Object get(String key, Class<?> resultClass) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            List<String> list = (ArrayList)jedis.eval(SCRIPT_LUA_CACHE_GET, 1, key );
            List records = new ArrayList() ;
            //IPage对象填充处理
            if ( list != null && !list.isEmpty()) {
                //使用MyBatisPlus的Page对象的属性名来比对
                if (list.get(0).contains("total") && list.get(0).contains("size") && list.get(0).contains("current") && list.get(0).contains("orders")) {
                    IPage result = jsonUtil.str2Obj(list.get(0), Page.class);
                    result.setRecords(records) ;
                    //跳过第一位数据的填充
                    for (int i = 1; i < list.size(); i++) {
                        records.add(jsonUtil.str2Obj(list.get(i),resultClass)) ;
                    }
                    //由于Redis内list的存储是类似栈帧压入的形式导致list存取时倒叙，所以此处取出时将顺序颠倒回原位
                    Collections.reverse(records);
                    return result ;
                } else if (list.get(0).equals("[]")) {
                    List result = new ArrayList() ;
                    //跳过第一位数据的填充
                    for (int i = 1; i < list.size(); i++) {
                        records.add(jsonUtil.str2Obj(list.get(i),resultClass)) ;
                    }
                    //由于Redis内list的存储是类似栈帧压入的形式导致list存取时倒叙，所以此处取出时将顺序颠倒回原位
                    Collections.reverse(records);
                    return result ;
                } else {
                    return jsonUtil.str2Obj(list.get(0), resultClass);
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
            Object targer = jsonUtil.str2Obj(jedis.get(id),result.getClass());
            BeanUtil.copyProperties(result, targer,
                    true, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
            jedis.setex(id, kacheConfig.getCacheTime(),jsonUtil.obj2Str(targer)) ;
            return (T) targer;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            close(jedis);
        }
        return null ;
    }
}
