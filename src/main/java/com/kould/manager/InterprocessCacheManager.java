package com.kould.manager;

import com.kould.config.InterprocessCacheProperties;
import com.kould.handler.StrategyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

public abstract class InterprocessCacheManager {
    @Autowired
    protected StrategyHandler strategyHandler;

    @Autowired
    protected InterprocessCacheProperties interprocessCacheProperties ;

    public abstract <T> T update(String key,T result, Class<?> beanClass) ;
    public abstract Object get(String key, Class<?> beanClass) throws ExecutionException;
    public abstract void clear(Class<?> beanClass) throws ExecutionException;
    public abstract <T> T put(String key, T result, Class<?> beanClass) throws ExecutionException;
}
