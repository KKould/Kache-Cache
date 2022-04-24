package com.kould.manager.impl;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.kould.manager.InterprocessCacheManager;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/*
使用Google的Guava Cache实现的进程缓存
用于缓存RemoteCache获取的缓存
仅为Key->Value形式存储
 */
public class GuavaCacheManager extends InterprocessCacheManager {
    
    private static final GuavaCacheManager INSTANCE = new GuavaCacheManager() ;

    //维护各个业务的进程间缓存Cache:
    //  Key:DTO名-》Value:Cache<String,Object>
    private static final Map<String, Cache<String,Object>> GUAVA_CACHE_MAP = new ConcurrentHashMap<>();

    private GuavaCacheManager() {}

    public static GuavaCacheManager getInstance() {
        return INSTANCE ;
    }
    
    @Override
    public <T> T update(String key, T result, String types) {

        return result;
    }

    @Override
    public Object get(String key, String types) {
        Cache<String,Object> cache = GUAVA_CACHE_MAP.get(types);
        if (cache == null) {
            return null ;
        } else {
            try {
                return cache.get(key, () -> null) ;
            } catch (Exception e) {
                return null ;
            }
        }
    }

    @Override
    public void clear(String types) {
        Cache<String,Object> cache = GUAVA_CACHE_MAP.get(types);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    @Override
    public <T> T put(String key, T result,String types) {
        Cache<String,Object> cache = GUAVA_CACHE_MAP.get(types);
        if (cache == null) {
            cache = CacheBuilder.newBuilder()
                    .weakValues()
                    .expireAfterAccess(strategyHandler.getCacheTime(),TimeUnit.SECONDS)
                    .maximumSize(interprocessCacheProperties.getSize())
                    .build() ;
            GUAVA_CACHE_MAP.put(types, cache);
        }
        if (result != null) {
            cache.put(key,result);
        }
        return result;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
