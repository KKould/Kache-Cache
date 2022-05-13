package com.kould.handler;

import com.kould.manager.IBaseCacheManager;
import com.kould.properties.DaoProperties;
import com.kould.entity.KacheMessage;

public abstract class AsyncHandler extends StrategyHandler {

    protected AsyncHandler(DaoProperties daoProperties, IBaseCacheManager cacheLogic) {
        super(daoProperties, cacheLogic);
    }

    public abstract void listen2Delete(KacheMessage msg) throws Exception;
    public abstract void listen2Update(KacheMessage msg) throws Exception;
    public abstract void listen2Insert(KacheMessage msg) throws Exception;
}
