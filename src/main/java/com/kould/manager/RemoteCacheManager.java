package com.kould.manager;

import com.kould.config.DataFieldProperties;
import com.kould.handler.StrategyHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class RemoteCacheManager {

    @Autowired
    protected StrategyHandler strategyHandler;

    @Autowired
    protected DataFieldProperties dataFieldProperties ;

    public abstract String getNullTag() ;

    public abstract Object put(String key, String lockKey, ProceedingJoinPoint point) throws Throwable;
    public abstract Boolean delKeys(String pattern) throws Throwable;
    public abstract Long del(String... keys) throws Throwable;
    public abstract Object updateById(String id,Object result) throws Throwable;
    public abstract Object get(String key, String lockKey) throws Throwable;

    public abstract Object unLockGet(String key) throws Throwable;
    public abstract Object unLockPut(String key, ProceedingJoinPoint point) throws Throwable;
}
