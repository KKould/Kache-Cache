package com.kould.handler;

import com.kould.properties.DaoProperties;
import com.kould.logic.CacheLogic;

public abstract class SyncHandler extends StrategyHandler{
    public SyncHandler(DaoProperties daoProperties, CacheLogic cacheLogic) {
        super(daoProperties, cacheLogic);
    }
}
