package com.kould.logic;

import com.kould.encoder.CacheEncoder;
import com.kould.lock.KacheLock;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.enity.KacheMessage;

public abstract class CacheLogic {

    protected KacheLock kacheLock ;

    protected CacheEncoder cacheEncoder ;

    protected RemoteCacheManager remoteCacheManager;

    protected InterprocessCacheManager interprocessCacheManager ;

    public CacheLogic(KacheLock kacheLock, CacheEncoder cacheEncoder, RemoteCacheManager remoteCacheManager,
                      InterprocessCacheManager interprocessCacheManager) {
        this.kacheLock = kacheLock;
        this.cacheEncoder = cacheEncoder;
        this.remoteCacheManager = remoteCacheManager;
        this.interprocessCacheManager = interprocessCacheManager;
    }

    public abstract void deleteRemoteCache(KacheMessage msg) throws Throwable;

    public abstract void deleteInterprocessCache(KacheMessage msg) throws Exception;

    public abstract void updateRemoteCache(KacheMessage msg) throws Exception, Throwable;

    public abstract void updateInterprocessCache(KacheMessage msg) throws Exception;

    public abstract void insertRemoteCache(KacheMessage msg) throws Throwable;

    public abstract void insertInterprocessCache(KacheMessage msg) throws Exception;
}
