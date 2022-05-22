package com.kould.manager;

import com.kould.properties.DaoProperties;
import com.kould.properties.InterprocessCacheProperties;

import java.util.concurrent.ExecutionException;

/*
进程间缓存，用于减少冗余的网络IO，提高单次响应时间
 */
public abstract class InterprocessCacheManager {

    protected DaoProperties daoProperties;

    protected InterprocessCacheProperties interprocessCacheProperties ;

    public InterprocessCacheManager(DaoProperties daoProperties, InterprocessCacheProperties interprocessCacheProperties) {
        this.daoProperties = daoProperties;
        this.interprocessCacheProperties = interprocessCacheProperties;
    }
    public abstract Object get(String key, String types) throws ExecutionException;
    public abstract void clear(String types);
    public abstract void put(String key, Object result, String types);
}
