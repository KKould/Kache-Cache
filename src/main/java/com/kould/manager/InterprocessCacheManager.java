package com.kould.manager;

import com.kould.api.BeanLoad;
import com.kould.properties.DaoProperties;
import com.kould.properties.InterprocessCacheProperties;

import java.util.concurrent.ExecutionException;

/*
进程间缓存，用于减少冗余的网络IO，提高单次响应时间
 */
public abstract class InterprocessCacheManager implements BeanLoad {

    protected DaoProperties daoProperties;

    protected InterprocessCacheProperties interprocessCacheProperties ;

    public abstract Object get(String key, String type) throws ExecutionException;
    public abstract void clear(String type);
    public abstract void put(String key, Object result, String type);

    @Override
    public Class<?>[] loadArgs() {
        return new Class[] {DaoProperties.class, InterprocessCacheProperties.class};
    }
}
