package com.kould.logic;

import com.kould.properties.DataFieldProperties;
import com.kould.encoder.CacheEncoder;
import com.kould.lock.KacheLock;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.entity.KacheMessage;

public abstract class CacheLogic {

    protected final KacheLock kacheLock ;

    protected final CacheEncoder cacheEncoder ;

    protected final RemoteCacheManager remoteCacheManager;

    protected final InterprocessCacheManager interprocessCacheManager ;

    protected final DataFieldProperties dataFieldProperties;

    protected CacheLogic(KacheLock kacheLock, CacheEncoder cacheEncoder, RemoteCacheManager remoteCacheManager,
                         InterprocessCacheManager interprocessCacheManager, DataFieldProperties dataFieldProperties) {
        this.kacheLock = kacheLock;
        this.cacheEncoder = cacheEncoder;
        this.remoteCacheManager = remoteCacheManager;
        this.interprocessCacheManager = interprocessCacheManager;
        this.dataFieldProperties = dataFieldProperties;
    }

    public abstract void deleteRemoteCache(KacheMessage msg) throws Exception;

    public abstract void deleteInterprocessCache(KacheMessage msg) throws Exception;

    public abstract void updateRemoteCache(KacheMessage msg) throws Exception, Exception;

    public abstract void updateInterprocessCache(KacheMessage msg) throws Exception;

    public abstract void insertRemoteCache(KacheMessage msg) throws Exception;

    public abstract void insertInterprocessCache(KacheMessage msg) throws Exception;
}
