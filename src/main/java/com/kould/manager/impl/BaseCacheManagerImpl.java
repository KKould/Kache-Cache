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
    public Object put(String key, Class<?> beanClass, String lockKey, ProceedingJoinPoint point) throws Throwable {
        Object result = remoteCacheManager.put(key, lockKey, point);
        if (interprocessCacheProperties.isEnable()) {
            interprocessCacheManager.put(key, result, beanClass) ;
        }
        return result;
    }

    @Override
    public Object get(String key, Class<?> beanClass, String lockKey) throws ExecutionException, NoSuchFieldException, IllegalAccessException {
        Object result = null ;
        if (interprocessCacheProperties.isEnable()) {
            result =interprocessCacheManager.get(key, beanClass) ;
        }
        if (result == null) {
            result = remoteCacheManager.get(key, beanClass, lockKey) ;
            if (interprocessCacheProperties.isEnable()) {
                if (result != null) {
                    interprocessCacheManager.put(key, result, beanClass) ;
                }
            }
        }
        return result ;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
