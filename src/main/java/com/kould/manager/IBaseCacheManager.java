package com.kould.manager;

import com.kould.encoder.CacheEncoder;
import com.kould.entity.KacheMessage;
import com.kould.properties.DataFieldProperties;
import com.kould.properties.InterprocessCacheProperties;
import com.kould.entity.MethodPoint;

/**
 * 二级缓存管理器
 *
 * 使用异步消息队列策略实现删改逻辑时请让增删改逻辑能保证幂等性
 */
public abstract class IBaseCacheManager {

    public IBaseCacheManager(InterprocessCacheManager interprocessCacheManager, RemoteCacheManager remoteCacheManager, InterprocessCacheProperties interprocessCacheProperties, CacheEncoder cacheEncoder, DataFieldProperties dataFieldProperties) {
        this.interprocessCacheManager = interprocessCacheManager;
        this.remoteCacheManager = remoteCacheManager;
        this.interprocessCacheProperties = interprocessCacheProperties;
        this.cacheEncoder = cacheEncoder;
        this.dataFieldProperties = dataFieldProperties;
    }

    protected InterprocessCacheManager interprocessCacheManager ;

    protected RemoteCacheManager remoteCacheManager ;

    protected InterprocessCacheProperties interprocessCacheProperties ;

    protected final CacheEncoder cacheEncoder ;

   protected final DataFieldProperties dataFieldProperties;
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

    public abstract void deleteCache(KacheMessage msg) throws Exception;

    public abstract void updateCache(KacheMessage msg) throws Exception;

    public abstract void insertCache(KacheMessage msg) throws Exception;
}
