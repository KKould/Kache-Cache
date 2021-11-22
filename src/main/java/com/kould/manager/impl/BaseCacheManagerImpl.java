package com.kould.manager.impl;

import com.kould.config.InterprocessCacheProperties;
import com.kould.manager.IBaseCacheManager;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

public class BaseCacheManagerImpl implements IBaseCacheManager {

    private static final Logger log = LoggerFactory.getLogger(IBaseCacheManager.class) ;

    @Autowired
    private InterprocessCacheManager interprocessCacheManager ;

    @Autowired
    private RemoteCacheManager remoteCacheManager ;

    @Autowired
    private InterprocessCacheProperties interprocessCacheProperties ;


    @Override
    public String getNullTag() {
        return remoteCacheManager.getNullTag() ;
    }

    @Override
    public String getNullValue() {
        return remoteCacheManager.getNullValue() ;
    }

    @Override
    public <T> T put(String key, T result, Class<?> beanClass) throws ExecutionException {
        if (interprocessCacheProperties.isEnable()) {
            interprocessCacheManager.put(key, result, beanClass) ;
        }
        return remoteCacheManager.put(key, result);
    }

    @Override
    public Object get(String key, Class<?> beanClass) throws ExecutionException {

        Object result = null ;
        if (interprocessCacheProperties.isEnable()) {
            result =interprocessCacheManager.get(key, beanClass) ;
            log.info("----------------------------------\r\n ++++ KaChe ++++ 从进程间缓存获取数据中");
        }
        if (result == null) {
            log.info("----------------------------------\r\n ++++ KaChe ++++ 从Redis缓存获取数据中");
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
}
