package com.kould.manager.impl;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.kould.config.DaoProperties;
import com.kould.config.InterprocessCacheProperties;
import com.kould.entity.NullValue;
import com.kould.manager.InterprocessCacheManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/*
使用Google的Guava Cache实现的进程缓存
用于缓存RemoteCache获取的缓存
仅为Key->Value形式存储
 */
public class GuavaCacheManager extends InterprocessCacheManager {

    //维护各个业务的进程间缓存Cache:
    //  Key:DTO名-》Value:Cache<String,Object>
    private static final Map<String, Cache<String,Object>> GUAVA_CACHE_MAP = new ConcurrentHashMap<>();

    public GuavaCacheManager(DaoProperties daoProperties, InterprocessCacheProperties interprocessCacheProperties) {
        super(daoProperties, interprocessCacheProperties);
    }

    @Override
    public Object get(String key, String types) throws ExecutionException {
        Cache<String,Object> cache = GUAVA_CACHE_MAP.get(types);
        if (cache != null) {
            Object result = cache.get(key, () -> NullValue.NULL_VALUE);
            if (result instanceof NullValue) {
                return null;
            } else {
                return result;
            }
        }
        return null;
    }

    @Override
    public void clear(String types) {
        Cache<String,Object> cache = GUAVA_CACHE_MAP.get(types);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    @Override
    public void put(String key, Object result,String types) {
        Cache<String,Object> cache = GUAVA_CACHE_MAP.computeIfAbsent(types, (k) -> CacheBuilder.newBuilder()
                .weakValues()
                .expireAfterAccess(daoProperties.getCacheTime(),TimeUnit.SECONDS)
                .maximumSize(interprocessCacheProperties.getSize())
                .build());
        cache.put(key,result);
    }
}
