package com.kould.manager;

import com.kould.properties.DaoProperties;
import com.kould.properties.DataFieldProperties;
import com.kould.entity.MethodPoint;

public abstract class RemoteCacheManager {

    protected DataFieldProperties dataFieldProperties;

    protected DaoProperties daoProperties;

    protected RemoteCacheManager(DataFieldProperties dataFieldProperties, DaoProperties daoProperties) {
        this.daoProperties = daoProperties;
        this.dataFieldProperties = dataFieldProperties;
    }

    public abstract String getNullTag() ;

    public abstract Object put(String key, String type, MethodPoint point) throws Exception;
    public abstract Long delKeys(String pattern) throws Exception;
    public abstract Long del(String... keys) throws Exception;
    public abstract Object get(String key) throws Exception;
    public abstract void init() throws Exception;
}
