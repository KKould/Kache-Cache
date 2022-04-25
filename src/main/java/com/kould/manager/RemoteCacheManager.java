package com.kould.manager;

import com.kould.config.DaoProperties;
import com.kould.config.DataFieldProperties;
import org.aspectj.lang.ProceedingJoinPoint;

public abstract class RemoteCacheManager {

    protected DataFieldProperties dataFieldProperties;

    protected DaoProperties daoProperties;

    public RemoteCacheManager(DataFieldProperties dataFieldProperties, DaoProperties daoProperties) {
        this.daoProperties = daoProperties;
        this.dataFieldProperties = dataFieldProperties;
    }

    public abstract String getNullTag() ;

    public abstract Object put(String key, String types, ProceedingJoinPoint point) throws Throwable;
    public abstract Boolean delKeys(String pattern) throws Throwable;
    public abstract Long del(String... keys) throws Throwable;
    public abstract Object updateById(String id,String type,Object result) throws Throwable;
    public abstract Object get(String key, String lockKey) throws Throwable;
}
