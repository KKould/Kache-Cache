package com.kould.manager.impl;

import com.kould.properties.InterprocessCacheProperties;
import com.kould.manager.IBaseCacheManager;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.entity.MethodPoint;

/*
此处进程间缓存并不与远程缓存做同一读写操作锁，通过牺牲一部分数据一致性换取最小的网络IO消耗
若是需要较强的数据一致性，则需要取消启用进程间缓存
 */
public class BaseCacheManagerImpl extends IBaseCacheManager {

    public BaseCacheManagerImpl(InterprocessCacheManager interprocessCacheManager, RemoteCacheManager remoteCacheManager
            , InterprocessCacheProperties interprocessCacheProperties) {
        super(interprocessCacheManager, remoteCacheManager, interprocessCacheProperties);
    }

    @Override
    public Object daoWrite(String key, MethodPoint point, String types) throws Exception {
        Object result = remoteCacheManager.put(key, types, point);
        if (interprocessCacheProperties.isEnable()) {
            interprocessCacheManager.put(key, result, types) ;
        }
        return result;
    }

    @Override
    public Object daoRead(String key, String types) throws Exception {
        Object result = null ;
        if (interprocessCacheProperties.isEnable()) {
            result =interprocessCacheManager.get(key, types) ;
        }
        if (result == null) {
            result = remoteCacheManager.get(key, types) ;
            if (interprocessCacheProperties.isEnable() && result != null) {
                interprocessCacheManager.put(key, result, types) ;
            }
        }
        return result ;
    }
}
