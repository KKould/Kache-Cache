package com.kould.handler.impl;

import com.kould.config.DaoProperties;
import com.kould.handler.SyncHandler;
import com.kould.logic.CacheLogic;
import com.kould.entity.KacheMessage;
import com.kould.proxy.MethodPoint;

public class DBFirstHandler extends SyncHandler {

    public DBFirstHandler(DaoProperties daoProperties, CacheLogic cacheLogic) {
        super(daoProperties, cacheLogic);
    }

    @Override
    public Object delete(MethodPoint point, KacheMessage serviceMessage) throws Throwable {
        Object proceed = point.execute();
        cacheLogic.deleteRemoteCache(serviceMessage);
        cacheLogic.deleteInterprocessCache(serviceMessage);
        return proceed;
    }

    @Override
    public Object update(MethodPoint point, KacheMessage serviceMessage) throws Throwable {
        Object proceed = point.execute();
        cacheLogic.updateRemoteCache(serviceMessage);
        cacheLogic.updateInterprocessCache(serviceMessage);
        return proceed;
    }

    @Override
    public Object insert(MethodPoint point, KacheMessage serviceMessage) throws Throwable {
        Object proceed = point.execute();
        cacheLogic.insertRemoteCache(serviceMessage);
        cacheLogic.insertInterprocessCache(serviceMessage);
        return proceed;
    }
}
