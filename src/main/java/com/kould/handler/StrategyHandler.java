package com.kould.handler;

import com.kould.config.DaoProperties;
import com.kould.logic.CacheLogic;
import com.kould.message.KacheMessage;
import org.aspectj.lang.ProceedingJoinPoint;

//负责缓存删除/更新处理与缓存存活时间策略
public abstract class StrategyHandler {

    public StrategyHandler(DaoProperties daoProperties, CacheLogic cacheLogic) {
        this.daoProperties = daoProperties;
        this.cacheLogic = cacheLogic;
    }

    protected DaoProperties daoProperties;

    protected CacheLogic cacheLogic;

    public abstract Object delete(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable;
    public abstract Object update(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable;
    public abstract Object insert(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable;
}
