package com.kould.logic;

import com.kould.encoder.CacheEncoder;
import com.kould.lock.KacheLock;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.message.KacheMessage;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CacheLogic {
    @Autowired
    protected KacheLock kacheLock ;

    @Autowired
    protected CacheEncoder cacheEncoder ;

    @Autowired
    protected RemoteCacheManager remoteCacheManager;

    @Autowired
    protected InterprocessCacheManager interprocessCacheManager ;

    public abstract void deleteRemoteCache(KacheMessage msg) throws Exception;

    public abstract void deleteInterprocessCache(KacheMessage msg) throws Exception;

    public abstract void updateRemoteCache(KacheMessage msg) throws Exception;

    public abstract void updateInterprocessCache(KacheMessage msg) throws Exception;

    public abstract void insertRemoteCache(KacheMessage msg) throws Exception;

    public abstract void insertInterprocessCache(KacheMessage msg) throws Exception;
}
