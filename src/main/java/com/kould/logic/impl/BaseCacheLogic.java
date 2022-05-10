package com.kould.logic.impl;

import com.kould.api.Kache;
import com.kould.properties.DataFieldProperties;
import com.kould.encoder.CacheEncoder;
import com.kould.lock.KacheLock;
import com.kould.logic.CacheLogic;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.entity.KacheMessage;
import com.kould.utils.FieldUtils;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;

//默认数据插入逻辑与数据删除逻辑一致
public class BaseCacheLogic extends CacheLogic {

    public BaseCacheLogic(KacheLock kacheLock, CacheEncoder cacheEncoder, RemoteCacheManager remoteCacheManager
            , InterprocessCacheManager interprocessCacheManager , DataFieldProperties dataFieldProperties) {
        super(kacheLock, cacheEncoder, remoteCacheManager, interprocessCacheManager, dataFieldProperties);
    }

    public void deleteRemoteCache(KacheMessage msg) throws Exception {
        deleteCacheByKey(msg);
    }

    public void deleteInterprocessCache(KacheMessage msg) throws ExecutionException {
        interprocessCacheClear(msg,kacheLock,interprocessCacheManager);
    }

    public void updateRemoteCache(KacheMessage msg) throws Exception {
        Class<?> resultClass = msg.getCacheClazz();
        String type = resultClass.getTypeName();
        Object arg = msg.getArg()[0];
        Class<?> argClass = arg.getClass();
        Lock writeLock = null;
        try {
            writeLock = kacheLock.writeLock(type);
            //==========上为重复代码
            //无法进行抽取的原因是因为lambda表达式无法抛出异常
            if (resultClass.isAssignableFrom(argClass)){
                String idStr = FieldUtils.getFieldByNameAndClass(argClass, dataFieldProperties.getPrimaryKeyName())
                        .get(arg).toString();
                remoteCacheManager.updateById(cacheEncoder.getId2Key(idStr, type), type, arg) ;
            }
            //==========下为重复代码
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

    public void updateInterprocessCache(KacheMessage msg) throws ExecutionException {
        interprocessCacheClear(msg,kacheLock,interprocessCacheManager);
    }

    @Override
    public void insertRemoteCache(KacheMessage msg) throws Exception {
        deleteCacheByKey(msg);
    }

    @Override
    public void insertInterprocessCache(KacheMessage msg) throws Exception {
        interprocessCacheClear(msg,kacheLock,interprocessCacheManager);
    }

    private void deleteCacheByKey(KacheMessage msg) throws Exception {
        Class<?> resultClass = msg.getCacheClazz();
        String lockKey = msg.getCacheClazz().getTypeName();
        Object arg = msg.getArg()[0];
        Class<?> argClass = arg.getClass();
        String type = msg.getTypes();
        Lock writeLock = null;
        try {
            writeLock = kacheLock.writeLock(lockKey);
            //==========上为重复代码
            //无法进行抽取的原因是因为lambda表达式无法抛出异常
            if (arg instanceof Serializable) {
                remoteCacheManager.del(cacheEncoder.getId2Key(arg.toString(), type));
            } else if (resultClass.isAssignableFrom(argClass)){
                String idStr = FieldUtils.getFieldByNameAndClass(argClass, dataFieldProperties.getPrimaryKeyName())
                        .get(arg).toString();
                remoteCacheManager.del(cacheEncoder.getId2Key(idStr, type));
            }
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

    private void interprocessCacheClear(KacheMessage msg, KacheLock kacheLock, InterprocessCacheManager interprocessCacheManager) throws ExecutionException {
        String lockKey = msg.getCacheClazz().getTypeName();
        Lock writeLock = null ;
        try {
            writeLock = kacheLock.writeLock(lockKey);
            interprocessCacheManager.clear(lockKey);
            kacheLock.unLock(writeLock);
        } catch (Exception e) {
            if (Boolean.TRUE.equals(kacheLock.isLockedByThisThread(writeLock))) {
                kacheLock.unLock(writeLock);
            }
            e.printStackTrace();
            throw e ;
        }
    }
}
