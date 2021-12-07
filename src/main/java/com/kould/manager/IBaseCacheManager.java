package com.kould.manager;

import com.kould.config.InterprocessCacheProperties;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class IBaseCacheManager {
    @Autowired
    protected InterprocessCacheManager interprocessCacheManager ;

    @Autowired
    protected RemoteCacheManager remoteCacheManager ;

    @Autowired
    protected InterprocessCacheProperties interprocessCacheProperties ;

    public abstract String getNullTag() ;
    public abstract String getNullValue() ;
    public abstract <T> T put(String key, T result, Class<?> beanClass) throws Exception;
    public abstract Object get(String key, Class<?> beanClass) throws Exception;
    public abstract boolean hasKey(String key) ;
}
