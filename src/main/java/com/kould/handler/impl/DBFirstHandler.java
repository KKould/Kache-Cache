package com.kould.handler.impl;

import com.kould.config.DaoProperties;
import com.kould.handler.SyncHandler;
import com.kould.logic.CacheLogic;
import com.kould.message.KacheMessage;
import org.aspectj.lang.ProceedingJoinPoint;

public class DBFirstHandler extends SyncHandler {

    public DBFirstHandler(DaoProperties daoProperties, CacheLogic cacheLogic) {
        super(daoProperties, cacheLogic);
    }

    @Override
    public Object delete(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable {
        Object proceed = point.proceed();
        cacheLogic.deleteRemoteCache(serviceMessage);
        cacheLogic.deleteInterprocessCache(serviceMessage);
        return proceed;
    }

    @Override
    public Object update(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable {
        Object proceed = point.proceed();
        cacheLogic.updateRemoteCache(serviceMessage);
        cacheLogic.updateInterprocessCache(serviceMessage);
        return proceed;
    }

    @Override
    public Object insert(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable {
        Object proceed = point.proceed();
        cacheLogic.insertRemoteCache(serviceMessage);
        cacheLogic.insertInterprocessCache(serviceMessage);
        return proceed;
    }
}
