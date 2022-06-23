package com.kould.core;

import com.kould.api.BeanLoad;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.Status;
import com.kould.entity.MethodPoint;
import com.kould.manager.IBaseCacheManager;
import com.kould.properties.ListenerProperties;

public abstract class CacheHandler implements BeanLoad {

    protected IBaseCacheManager baseCacheManager;

    protected CacheEncoder encoder;

    protected ListenerProperties listenerProperties;

    public abstract Object load(MethodPoint point, String types, Status methodStatus) throws Throwable;

    @Override
    public Class<?>[] loadArgs() {
        return new Class[]{IBaseCacheManager.class, CacheEncoder.class, ListenerProperties.class};
    }
}
