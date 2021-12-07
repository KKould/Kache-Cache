package com.kould.manager.impl;
import com.kould.manager.IBaseCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class BaseCacheManagerImpl extends IBaseCacheManager {

    private static final BaseCacheManagerImpl INSTANCE = new BaseCacheManagerImpl() ;

    private BaseCacheManagerImpl() {}

    public static BaseCacheManagerImpl getInstance() {
        return INSTANCE ;
    }

    @Override
    public String getNullTag() {
        return remoteCacheManager.getNullTag() ;
    }

    @Override
    public String getNullValue() {
        return remoteCacheManager.getNullValue() ;
    }

    @Override
    public <T> T put(String key, T result, Class<?> beanClass) throws Exception {
        if (interprocessCacheProperties.isEnable()) {
            interprocessCacheManager.put(key, result, beanClass) ;
        }
        return remoteCacheManager.put(key, result);
    }

    @Override
    public Object get(String key, Class<?> beanClass) throws Exception {

        Object result = null ;
        if (interprocessCacheProperties.isEnable()) {
            result =interprocessCacheManager.get(key, beanClass) ;
        }
        if (result == null) {
            result = remoteCacheManager.get(key, beanClass) ;
            if (interprocessCacheProperties.isEnable()) {
                if (result != null) {
                    interprocessCacheManager.put(key, result, beanClass) ;
                }
            }
        }
        return result ;
    }

    @Override
    public boolean hasKey(String key) {
        return remoteCacheManager.hasKey(key);
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
