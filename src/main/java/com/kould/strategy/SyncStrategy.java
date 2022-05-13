package com.kould.strategy;

import com.kould.manager.IBaseCacheManager;

public abstract class SyncStrategy extends Strategy {
    public SyncStrategy(IBaseCacheManager cacheLogic) {
        super(cacheLogic);
    }
}
