package com.kould.manager;

import com.kould.api.BeanLoad;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.PageDetails;
import com.kould.properties.DaoProperties;
import com.kould.entity.MethodPoint;

public abstract class RemoteCacheManager implements BeanLoad {

    protected DaoProperties daoProperties;

    protected CacheEncoder cacheEncoder;

    public abstract String getNullTag() ;

    public abstract Object put(String key, String type, MethodPoint point, PageDetails<?> pageDetails) throws Throwable;
    public abstract Long delKeys(String pattern) throws Throwable;
    public abstract Long del(String... keys) throws Throwable;
    public abstract Object get(String key, PageDetails<?> pageDetails) throws Throwable;
    public abstract boolean cas(String key) throws Throwable;
    public abstract void init() throws Throwable;

    @Override
    public Class<?>[] loadArgs() {
        return new Class[] {DaoProperties.class, CacheEncoder.class};
    }
}
