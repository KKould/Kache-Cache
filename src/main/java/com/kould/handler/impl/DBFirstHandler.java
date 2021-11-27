package com.kould.handler.impl;

import com.kould.handler.SyncHandler;
import com.kould.message.KacheMessage;
import org.aspectj.lang.ProceedingJoinPoint;

public class DBFirstHandler extends SyncHandler {

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

    @Override
    public int getCacheTime() {
        return (int) (daoProperties.getBaseTime() + Math.random() * daoProperties.getRandomTime());
    }
}
