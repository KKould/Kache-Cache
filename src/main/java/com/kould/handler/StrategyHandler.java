package com.kould.handler;

import com.kould.config.DaoProperties;
import com.kould.logic.CacheLogic;
import com.kould.message.KacheMessage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

//负责缓存删除/更新处理与缓存存活时间策略
public abstract class StrategyHandler {
    @Autowired
    protected DaoProperties daoProperties ;

    @Autowired
    protected CacheLogic cacheLogic ;

    public abstract Object delete(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable;
    public abstract Object update(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable;
    public abstract Object insert(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable;

    public abstract int getCacheTime() ;
}
