package com.kould.manager;

import com.kould.config.DataFieldProperties;
import com.kould.handler.StrategyHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class RemoteCacheManager {

    @Autowired
    protected StrategyHandler strategyHandler;

    @Autowired
    protected DataFieldProperties dataFieldProperties ;

    public abstract String getNullTag() ;
    public abstract Object getNullValue() ;

    public abstract Object put(String key, String lockKey, ProceedingJoinPoint point) throws Throwable;
    public abstract List<String> keys(String pattern) ;
    public abstract Long del(String... keys) ;
    public abstract <T> T updateById(String id,T result) ;
    public abstract Object get(String key, Class<?> beanClass, String lockKey) throws NoSuchFieldException, IllegalAccessException;
}
