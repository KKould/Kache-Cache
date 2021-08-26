package com.kould.manager.impl;

import com.kould.bean.KacheConfig;
import com.kould.manager.InterprocessCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class HashMapCacheManager implements InterprocessCacheManager {

    @Autowired
    private KacheConfig kacheConfig ;

    private static final Map<String,Object> cacheMap = new HashMap<>() ;

    private static final Map<Long, String> cacheTimeMap = new ConcurrentHashMap<>() ;

    @Override
    public <T> T put(String key, T result, int limit) {
        if (cacheMap.size() < limit) {
            cacheTimeMap.put(System.currentTimeMillis(), key) ;
            cacheMap.put(key,result) ;
            return result ;
        } else {
            cacheCleanByTime();
            if (cacheMap.size() < limit) {
                cacheMap.put(key,result) ;
                return result ;
            }
            cacheCleanByOrder();
            cacheMap.put(key,result) ;
            return result ;
        }
    }

    private void cacheCleanByTime() {
        log.info("\r\n 通过过期时间删除进程间缓存");
        cacheTimeMap.keySet().stream().forEach(key -> {
            if ((System.currentTimeMillis() - key) / 1000 > kacheConfig.getCacheTime()) {
                cacheMap.remove(cacheTimeMap.get(key)) ;
                cacheTimeMap.remove(key) ;
            };
        });
    }

    private void cacheCleanByOrder() {
        log.info("\r\n 通过时间顺序删除进程间缓存");
        long minTime = Long.MAX_VALUE ;
        for (Long key : cacheTimeMap.keySet()) {
            if (key < minTime) {
                minTime = key ;
            }
        }
        cacheMap.remove(cacheTimeMap.get(minTime)) ;
        cacheTimeMap.remove(minTime) ;
    }

    @Override
    public boolean hasKey(String key) {
        return cacheMap.containsKey(key);
    }

    @Override
    public <T> T update(String key, T result) {
        cacheTimeMap.put(System.currentTimeMillis(), key) ;
        return (T) cacheMap.put(key,result);
    }

    @Override
    public Object get(String key) {
        return cacheMap.get(key);
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }
}
