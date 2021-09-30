package com.kould.manager.impl;

import com.kould.bean.KacheConfig;
import com.kould.manager.IBaseCacheManager;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class BaseCacheManagerImpl implements IBaseCacheManager {

    @Autowired
    private InterprocessCacheManager interprocessCacheManager ;

    @Autowired
    private RemoteCacheManager remoteCacheManager ;

    @Autowired
    private KacheConfig kacheConfig ;

    @Override
    public <T> T put(String key, T result, Class<?> beanClass) throws ExecutionException {
        if (kacheConfig.isEnableInterprocessCache()) {
            interprocessCacheManager.put(key, result, beanClass) ;
        }
        return remoteCacheManager.put(key, result);
    }

    @Override
    public Object get(String key, Class<?> resultClass, Class<?> beanClass) throws ExecutionException {

        Object result = null ;
        if (kacheConfig.isEnableInterprocessCache()) {
            result =interprocessCacheManager.get(key, beanClass) ;
            log.info("----------------------------------\r\n ++++ KaChe ++++ 从进程间缓存获取数据中");
        }
        if (result == null) {
            log.info("----------------------------------\r\n ++++ KaChe ++++ 从Redis缓存获取数据中");
            result = remoteCacheManager.get(key, resultClass, beanClass) ;
            if (kacheConfig.isEnableInterprocessCache()) {
                if (result != null) {
                    interprocessCacheManager.put(key, result, beanClass) ;
                }
            }
        }
        return result ;
    }
}
