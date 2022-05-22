package com.kould.manager.impl;

import com.kould.api.Kache;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.KacheMessage;
import com.kould.properties.DataFieldProperties;
import com.kould.properties.InterprocessCacheProperties;
import com.kould.manager.IBaseCacheManager;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.entity.MethodPoint;
import com.kould.utils.FieldUtils;

import java.io.Serializable;

public class BaseCacheManagerImpl extends IBaseCacheManager {


    public BaseCacheManagerImpl(InterprocessCacheManager interprocessCacheManager, RemoteCacheManager remoteCacheManager
            , InterprocessCacheProperties interprocessCacheProperties, CacheEncoder cacheEncoder, DataFieldProperties dataFieldProperties) {
        super(interprocessCacheManager, remoteCacheManager, interprocessCacheProperties, cacheEncoder, dataFieldProperties);
    }

    @Override
    public Object daoWrite(String key, MethodPoint point, String type) throws Exception {
        Object result = remoteCacheManager.put(key, type, point);
        if (interprocessCacheProperties.isEnable()) {
            interprocessCacheManager.put(key, result, type) ;
        }
        return result;
    }

    @Override
    public Object daoRead(String key, String type) throws Exception {
        Object result = null ;
        if (interprocessCacheProperties.isEnable()) {
            result =interprocessCacheManager.get(key, type) ;
        }
        if (result == null) {
            result = remoteCacheManager.get(key) ;
            if (interprocessCacheProperties.isEnable() && result != null) {
                interprocessCacheManager.put(key, result, type) ;
            }
        }
        return result ;
    }

    @Override
    public void deleteCache(KacheMessage msg) throws Exception {
        deleteCacheByKey(msg);
    }

    @Override
    public void updateCache(KacheMessage msg) throws Exception {
        deleteCacheByKey(msg);
    }

    @Override
    public void insertCache(KacheMessage msg) throws Exception {
        deleteCacheByKey(msg);
    }

    /**
     * 无锁删除索引缓存与元缓存方法
     *
     * 元缓存删除会导致到索引缓存收集，以此原理对元缓存优先处理使其未删除的索引缓存能够返回空而走数据库，
     * 保证其数据实时性，实现类似CAS的效果
     *
     * @param msg 方法摘要
     * @throws Exception
     */
    private void deleteCacheByKey(KacheMessage msg) throws Exception {
        Class<?> resultClass = msg.getCacheClazz();
        Object arg = msg.getArg()[0];
        Class<?> argClass = arg.getClass();
        String typeName = msg.getType();
        if (arg instanceof Serializable) {
            remoteCacheManager.del(cacheEncoder.getId2Key(arg.toString(), typeName));
        } else if (resultClass.isAssignableFrom(argClass)){
            String idStr = FieldUtils.getFieldByNameAndClass(argClass, dataFieldProperties.getPrimaryKeyName())
                    .get(arg).toString();
            remoteCacheManager.del(cacheEncoder.getId2Key(idStr, typeName));
        }
        interprocessCacheManager.clear(typeName);
        // 表达式中加*号使类型可以匹配多类型
        remoteCacheManager.delKeys(cacheEncoder.getPattern(Kache.INDEX_TAG + "*" + resultClass.getName()));
    }
}
