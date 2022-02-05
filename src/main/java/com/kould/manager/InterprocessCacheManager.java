package com.kould.manager;

import com.kould.config.InterprocessCacheProperties;
import com.kould.handler.StrategyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

/*
进程间缓存，用于减少冗余的网络IO，提高单次响应时间
为了减少锁粒度而牺牲了其线程安全性，若要求强一致性建议取消使用进程间缓存
 */
public abstract class InterprocessCacheManager {

    @Autowired
    protected StrategyHandler strategyHandler;

    @Autowired
    protected InterprocessCacheProperties interprocessCacheProperties ;

    public abstract <T> T update(String key,T result, String types) ;
    public abstract Object get(String key, String types);
    public abstract void clear(String types);
    public abstract <T> T put(String key, T result, String types);
}
