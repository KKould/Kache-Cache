package com.kould.handler.impl;

import com.kould.manager.IBaseCacheManager;
import com.kould.properties.DaoProperties;
import com.kould.handler.SyncHandler;
import com.kould.entity.KacheMessage;
import com.kould.entity.MethodPoint;

public class DBFirstHandler extends SyncHandler {

    public DBFirstHandler(DaoProperties daoProperties, IBaseCacheManager baseCacheManager) {
        super(daoProperties, baseCacheManager);
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
