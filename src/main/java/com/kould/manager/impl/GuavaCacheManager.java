package com.kould.manager.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.kould.bean.KacheConfig;
import com.kould.manager.InterprocessCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class GuavaCacheManager implements InterprocessCacheManager {

    @Autowired
    private KacheConfig kacheConfig ;

    private Cache<String,Cache<String,Object>> guavaCacheMap = CacheBuilder.newBuilder().weakValues().build() ;

    @Override
    public <T> T update(String key, T result, Class<?> resultClass) {

        return result;
    }

    @Override
    public Object get(String key, Class<?> resultClass) throws ExecutionException {
        String name = resultClass.getName();
        Cache<String,Object> cache = guavaCacheMap.get(name, new Callable<Cache<String,Object>>() {
            @Override
            public Cache<String,Object> call() throws Exception {
                return null;
            }
        });
        if (cache == null) {
            return null ;
        } else {
            return cache.get(key, new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return null;
                }
            }) ;
        }
    }

    @Override
    public void clear(Class<?> resultClass) throws ExecutionException {
        String name = resultClass.getName();
        Cache<String,Object> cache = guavaCacheMap.get(name, new Callable<Cache<String,Object>>() {
            @Override
            public Cache<String,Object> call() throws Exception {
                return null;
            }
        });
        if (cache != null) {
            cache.cleanUp();
        }
    }

    @Override
    public <T> T put(String key, T result, Class<?> resultClass) throws ExecutionException {
        String name = resultClass.getName();
        Cache<String,Object> cache = guavaCacheMap.get(name, new Callable<Cache<String,Object>>() {
            @Override
            public Cache<String,Object> call() throws Exception {
                return null;
            }
        });
        if (cache == null) {
            cache = CacheBuilder.newBuilder()
                    .weakValues()
                    .expireAfterAccess(kacheConfig.getCacheTime(),TimeUnit.SECONDS)
                    .maximumSize(kacheConfig.getInterprocessCacheSize())
                    .build() ;
            guavaCacheMap.put(name, cache);
        }
        cache.put(key,result);
        return result;
    }
}
