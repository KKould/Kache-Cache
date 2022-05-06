package com.kould.manager;

import com.kould.config.InterprocessCacheProperties;
import com.kould.proxy.MethodPoint;

import java.util.concurrent.ExecutionException;

public abstract class IBaseCacheManager {

    public IBaseCacheManager(InterprocessCacheManager interprocessCacheManager, RemoteCacheManager remoteCacheManager, InterprocessCacheProperties interprocessCacheProperties) {
        this.interprocessCacheManager = interprocessCacheManager;
        this.remoteCacheManager = remoteCacheManager;
        this.interprocessCacheProperties = interprocessCacheProperties;
    }

    protected InterprocessCacheManager interprocessCacheManager ;

    protected RemoteCacheManager remoteCacheManager ;

    protected InterprocessCacheProperties interprocessCacheProperties ;

    /**
     * 抽象层面上进行缓存的具体存储操作调控
     * 优先对远程缓存进行修改
     * @param key 缓存Key
     * @param point 切点
     * @param types 类类型
     * @return 缓存具体数据
     * @throws Exception
     */
    public abstract Object daoWrite(String key, MethodPoint point, String types) throws Exception;

    /**
     * 抽象层面上进行缓存的具体读取操作调控
     * 优先从进程间缓存获取数据（在进程缓存开启的情况下）
     * @param key 缓存Key
     * @param types 类类型
     * @return 缓存具体数据
     * @throws Exception
     */
    public abstract Object daoRead(String key, String types) throws Exception;
}
