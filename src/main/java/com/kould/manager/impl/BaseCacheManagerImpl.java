package com.kould.manager.impl;
import com.kould.manager.IBaseCacheManager;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.ExecutionException;

/*
此处进程间缓存并不与远程缓存做同一读写操作锁，通过牺牲一部分数据一致性换取最小的网络IO消耗
若是需要较强的数据一致性，则需要取消启用进程间缓存
 */
public class BaseCacheManagerImpl extends IBaseCacheManager {

    private static final BaseCacheManagerImpl INSTANCE = new BaseCacheManagerImpl() ;

    private BaseCacheManagerImpl() {}

    public static BaseCacheManagerImpl getInstance() {
        return INSTANCE ;
    }

    @Override
    public Object getNullValue() {
        return remoteCacheManager.getNullValue() ;
    }

    @Override
    public Object daoWrite(String key, ProceedingJoinPoint point, String types) throws Throwable {
        Object result = remoteCacheManager.put(key, types, point);
        if (interprocessCacheProperties.isEnable()) {
            interprocessCacheManager.put(key, result, types) ;
        }
        return result;
    }

    @Override
    public Object daoRead(String key, String types) throws ExecutionException, NoSuchFieldException, IllegalAccessException {
        Object result = null ;
        if (interprocessCacheProperties.isEnable()) {
            result =interprocessCacheManager.get(key, types) ;
        }
        if (result == null) {
            result = remoteCacheManager.get(key, types) ;
            if (interprocessCacheProperties.isEnable()) {
                if (result != null) {
                    interprocessCacheManager.put(key, result, types) ;
                }
            }
        }
        return result ;
    }

    @Override
    public Object serviceWrite(String key, ProceedingJoinPoint point, String types) {
        return remoteCacheManager.unLockPut(key,point);
    }

    @Override
    public Object serviceRead(String key, String types) {
        return remoteCacheManager.unLockGet(key);
    }


    private Object readResolve() {
        return INSTANCE;
    }
}
