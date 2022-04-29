package com.kould.handler;

import com.kould.config.DaoProperties;
import com.kould.logic.CacheLogic;
import com.kould.enity.KacheMessage;
import com.kould.proxy.MethodPoint;

//负责缓存删除/更新处理与缓存存活时间策略
public abstract class StrategyHandler {

    public StrategyHandler(DaoProperties daoProperties, CacheLogic cacheLogic) {
        this.daoProperties = daoProperties;
        this.cacheLogic = cacheLogic;
    }

    protected DaoProperties daoProperties;

    protected CacheLogic cacheLogic;

    public abstract Object delete(MethodPoint point, KacheMessage serviceMessage) throws Throwable;
    public abstract Object update(MethodPoint point, KacheMessage serviceMessage) throws Throwable;
    public abstract Object insert(MethodPoint point, KacheMessage serviceMessage) throws Throwable;
}
