package com.kould.manager;

import com.kould.config.DaoProperties;
import com.kould.config.InterprocessCacheProperties;

import java.util.concurrent.ExecutionException;

/*
进程间缓存，用于减少冗余的网络IO，提高单次响应时间
为了减少锁粒度而牺牲了其线程安全性，若要求强一致性建议取消使用进程间缓存
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
