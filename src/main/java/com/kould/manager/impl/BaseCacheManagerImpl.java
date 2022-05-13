package com.kould.manager.impl;

import com.kould.api.Kache;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.KacheMessage;
import com.kould.lock.KacheLock;
import com.kould.properties.DataFieldProperties;
import com.kould.properties.InterprocessCacheProperties;
import com.kould.manager.IBaseCacheManager;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.entity.MethodPoint;
import com.kould.utils.FieldUtils;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;

/*
此处进程间缓存并不与远程缓存做同一读写操作锁，通过牺牲一部分数据一致性换取最小的网络IO消耗
若是需要较强的数据一致性，则需要取消启用进程间缓存
 */
public class BaseCacheManagerImpl extends IBaseCacheManager {


    public BaseCacheManagerImpl(InterprocessCacheManager interprocessCacheManager, RemoteCacheManager remoteCacheManager
            , InterprocessCacheProperties interprocessCacheProperties, KacheLock kacheLock, CacheEncoder cacheEncoder, DataFieldProperties dataFieldProperties) {
        super(interprocessCacheManager, remoteCacheManager, interprocessCacheProperties, kacheLock, cacheEncoder, dataFieldProperties);
    }

    @Override
    public Object daoWrite(String key, MethodPoint point, String types) throws Exception {
        Object result = remoteCacheManager.put(key, types, point);
        if (interprocessCacheProperties.isEnable()) {
            interprocessCacheManager.put(key, result, types) ;
        }
        return result;
    }

    @Override
    public Object daoRead(String key, String types) throws Exception {
        Object result = null ;
        if (interprocessCacheProperties.isEnable()) {
            result =interprocessCacheManager.get(key, types) ;
        }
        if (result == null) {
            result = remoteCacheManager.get(key, types) ;
            if (interprocessCacheProperties.isEnable() && result != null) {
                interprocessCacheManager.put(key, result, types) ;
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
        Class<?> resultClass = msg.getCacheClazz();
        String typeName = msg.getTypes();
        Object arg = msg.getArg()[0];
        Class<?> argClass = arg.getClass();
        Lock writeLock = null;
        try {
            writeLock = kacheLock.writeLock(typeName);
            //==========上为重复代码
            //无法进行抽取的原因是因为lambda表达式无法抛出异常
            if (resultClass.isAssignableFrom(argClass)){
                String idStr = FieldUtils.getFieldByNameAndClass(argClass, dataFieldProperties.getPrimaryKeyName())
                        .get(arg).toString();
                remoteCacheManager.updateById(cacheEncoder.getId2Key(idStr, typeName), typeName, arg) ;
            }
            //==========下为重复代码
            interprocessCacheManager.clear(typeName);
            remoteCacheManager.delKeys(cacheEncoder.getPattern(resultClass.getName()));
            kacheLock.unLock(writeLock);
        } catch (Exception e){
            if (Boolean.TRUE.equals(kacheLock.isLockedByThisThread(writeLock))) {
                kacheLock.unLock(writeLock);
            }
            e.printStackTrace();
            throw e ;
        }
    }

    @Override
    public void insertCache(KacheMessage msg) throws Exception {
        deleteCacheByKey(msg);
    }

    private void deleteCacheByKey(KacheMessage msg) throws Exception {
        Class<?> resultClass = msg.getCacheClazz();
        Object arg = msg.getArg()[0];
        Class<?> argClass = arg.getClass();
        String typeName = msg.getTypes();
        Lock writeLock = null;
        try {
            writeLock = kacheLock.writeLock(typeName);
            //==========上为重复代码
            //无法进行抽取的原因是因为lambda表达式无法抛出异常
            if (arg instanceof Serializable) {
                remoteCacheManager.del(cacheEncoder.getId2Key(arg.toString(), typeName));
            } else if (resultClass.isAssignableFrom(argClass)){
                String idStr = FieldUtils.getFieldByNameAndClass(argClass, dataFieldProperties.getPrimaryKeyName())
                        .get(arg).toString();
                remoteCacheManager.del(cacheEncoder.getId2Key(idStr, typeName));
            }
            interprocessCacheManager.clear(typeName);
            remoteCacheManager.delKeys(cacheEncoder.getPattern(Kache.INDEX_TAG + resultClass.getName()));
            kacheLock.unLock(writeLock);
        } catch (Exception e){
            if (Boolean.TRUE.equals(kacheLock.isLockedByThisThread(writeLock))) {
                kacheLock.unLock(writeLock);
            }
            e.printStackTrace();
            throw e ;
        }
    }
}
