package com.kould.strategy.impl;

import com.kould.manager.IBaseCacheManager;
import com.kould.strategy.SyncStrategy;
import com.kould.entity.KacheMessage;
import com.kould.entity.MethodPoint;

public class DBFirstStrategy extends SyncStrategy {

    public DBFirstStrategy(IBaseCacheManager baseCacheManager) {
        super(baseCacheManager);
    }

    @Override
    public Object delete(MethodPoint point, KacheMessage message) throws Exception {
        Object proceed = point.execute();
        baseCacheManager.deleteCache(message);
        return proceed;
    }

    @Override
    public Object update(MethodPoint point, KacheMessage message) throws Exception {
        Object proceed = point.execute();
        baseCacheManager.updateCache(message);
        return proceed;
    }

    @Override
    public Object insert(MethodPoint point, KacheMessage message) throws Exception {
        Object proceed = point.execute();
        baseCacheManager.insertCache(message);
        return proceed;
    }
}
