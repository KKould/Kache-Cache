package com.kould.manager.impl;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.kould.config.DaoProperties;
import com.kould.config.InterprocessCacheProperties;
import com.kould.manager.InterprocessCacheManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuavaCacheManager implements InterprocessCacheManager {

    @Autowired
    private DaoProperties daoProperties ;

    @Autowired
    private InterprocessCacheProperties interprocessCacheProperties ;

    //维护各个业务的进程间缓存Cache:
    //  Key:DTO名-》Value:Cache<String,Object>
    private final Map<String, Cache<String,Object>> guavaCacheMap = new ConcurrentHashMap<>();

    @Override
    public <T> T update(String key, T result, Class<?> resultClass) {

        return result;
    }

    @Override
    public Object get(String key, Class<?> beanClass) throws ExecutionException {
        String name = beanClass.getName();
        Cache<String,Object> cache = guavaCacheMap.get(name);
        if (cache == null) {
            return null ;
        } else {
            try {
                return cache.get(key, new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return null;
                    }
                }) ;
            } catch (Exception e) {
                return null ;
            }
        }
    }

    @Override
    public void clear(Class<?> beanClass) throws ExecutionException {
        String name = beanClass.getName();
        Cache<String,Object> cache = guavaCacheMap.get(name);
        if (cache != null) {
            cache.cleanUp();
        }
    }

    @Override
    public <T> T put(String key, T result, Class<?> beanClass) throws ExecutionException {
        String name = beanClass.getName();
        Cache<String,Object> cache = guavaCacheMap.get(name);
        if (cache == null) {
            cache = CacheBuilder.newBuilder()
                    .weakValues()
                    .expireAfterAccess(daoProperties.getCacheTime(),TimeUnit.SECONDS)
                    .maximumSize(interprocessCacheProperties.getSize())
                    .build() ;
            guavaCacheMap.put(name, cache);
        }
        if (result != null) {
            cache.put(key,result);
        }
        return result;
    }
}
