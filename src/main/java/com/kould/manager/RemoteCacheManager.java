package com.kould.manager;

import com.kould.config.DaoProperties;
import com.kould.config.DataFieldProperties;
import com.kould.proxy.MethodPoint;

public abstract class RemoteCacheManager {

    protected DataFieldProperties dataFieldProperties;

    protected DaoProperties daoProperties;

    protected RemoteCacheManager(DataFieldProperties dataFieldProperties, DaoProperties daoProperties) {
        this.daoProperties = daoProperties;
        this.dataFieldProperties = dataFieldProperties;
    }

    public abstract String getNullTag() ;

    public abstract Object put(String key, String types, MethodPoint point) throws Throwable;
    public abstract Long delKeys(String pattern) throws Throwable;
    public abstract Long del(String... keys) throws Throwable;
    public abstract Object updateById(String id,String type,Object result) throws Throwable;
    public abstract Object get(String key, String lockKey) throws Throwable;
    public abstract void init() throws Exception;
}
