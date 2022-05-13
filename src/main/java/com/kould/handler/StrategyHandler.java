package com.kould.handler;

import com.kould.manager.IBaseCacheManager;
import com.kould.properties.DaoProperties;
import com.kould.entity.KacheMessage;
import com.kould.entity.MethodPoint;

//负责缓存删除/更新处理与缓存存活时间策略
public abstract class StrategyHandler {

    public StrategyHandler(DaoProperties daoProperties, IBaseCacheManager baseCacheManager) {
        this.daoProperties = daoProperties;
        this.baseCacheManager = baseCacheManager;
    }

    protected DaoProperties daoProperties;

    protected IBaseCacheManager baseCacheManager;

    public abstract Object delete(MethodPoint point, KacheMessage serviceMessage) throws Exception;
    public abstract Object update(MethodPoint point, KacheMessage serviceMessage) throws Exception;
    public abstract Object insert(MethodPoint point, KacheMessage serviceMessage) throws Exception;
}
