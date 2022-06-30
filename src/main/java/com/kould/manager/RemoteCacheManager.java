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

    public abstract <T> Object put(String key, String type, MethodPoint point, PageDetails<T> pageDetails) throws Exception;
    public abstract Long delKeys(String pattern) throws Exception;
    public abstract Long del(String... keys) throws Exception;
    public abstract Object get(String key, PageDetails<?> pageDetails) throws Exception;
    public abstract boolean cas(String key) throws Exception;
    public abstract void init() throws Exception;

    @Override
    public Class<?>[] loadArgs() {
        return new Class[] {DaoProperties.class, CacheEncoder.class};
    }
}
