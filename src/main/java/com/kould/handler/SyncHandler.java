package com.kould.handler;

import com.kould.manager.IBaseCacheManager;
import com.kould.properties.DaoProperties;

public abstract class SyncHandler extends StrategyHandler{
    public SyncHandler(DaoProperties daoProperties, IBaseCacheManager cacheLogic) {
        super(daoProperties, cacheLogic);
    }
}
