package com.kould.manager.impl;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.kould.entity.NullValue;
import com.kould.manager.LocalCacheManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/*
使用Google的Guava Cache实现的进程缓存
用于缓存RemoteCache获取的缓存
仅为Key->Value形式存储
 */
public class GuavaCacheManager extends LocalCacheManager {

    //维护各个业务的进程间缓存Cache:
    //  Key:DTO名-》Value:Cache<String,Object>
    private static final Map<String, LoadingCache<String,Object>> GUAVA_CACHE_MAP = new ConcurrentHashMap<>();

    @Override
    public Object get(String key, String type) throws ExecutionException {
        LoadingCache<String,Object> cache = GUAVA_CACHE_MAP.get(type);
        if (cache != null) {
            Object result = cache.get(key);
            if (!(result instanceof NullValue)) {
                return result;
            }
        }
        return null;
    }

    @Override
    public void clear(String type) {
        Cache<String,Object> cache = GUAVA_CACHE_MAP.get(type);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    @Override
    public void put(String key, Object result,String type) {
        LoadingCache<String,Object> cache = GUAVA_CACHE_MAP.computeIfAbsent(type, k -> CacheBuilder.newBuilder()
                .weakValues()
                .expireAfterAccess(daoProperties.getCacheTime(),TimeUnit.SECONDS)
                .maximumSize(localCacheProperties.getSize())
                .build(CacheLoader.from(NullValue::getInstance)));
        cache.put(key,result);
    }
}
