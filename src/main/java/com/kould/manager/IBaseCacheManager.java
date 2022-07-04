package com.kould.manager;

import com.kould.api.BeanLoad;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.PageDetails;
import com.kould.entity.KacheMessage;
import com.kould.lock.KacheLock;
import com.kould.properties.LocalCacheProperties;
import com.kould.entity.MethodPoint;

/**
 * 二级缓存管理器
 *
 * 使用异步消息队列策略实现删改逻辑时请让增删改逻辑能保证幂等性
 */
public abstract class IBaseCacheManager implements BeanLoad {

    protected LocalCacheManager localCacheManager;

    protected RemoteCacheManager remoteCacheManager ;

    protected LocalCacheProperties localCacheProperties;

    protected CacheEncoder cacheEncoder ;

    protected PageDetails<?> pageDetails;

    protected KacheLock kacheLock;
    /**
     * 抽象层面上进行缓存的具体存储操作调控
     * 优先对远程缓存进行修改
     * @param key 缓存Key
     * @param point 切点
     * @param type 类类型
     * @return 缓存具体数据
     * @throws RuntimeException 方法拦截时被代理方法可能产生的异常
     */
    public abstract Object daoWrite(String key, MethodPoint point, String type) throws Exception;

    /**
     * 抽象层面上进行缓存的具体读取操作调控
     * 优先从进程间缓存获取数据（在进程缓存开启的情况下）
     * @param key 缓存Key
     * @param type 类类型
     * @return 缓存具体数据
     * @throws Exception 方法拦截时被代理方法可能产生的异常
     */
    public abstract Object daoRead(String key, String type) throws Exception;

    public abstract void deleteCache(KacheMessage msg) throws Exception;

    public abstract void updateCache(KacheMessage msg) throws Exception;

    public abstract void insertCache(KacheMessage msg) throws Exception;

    @Override
    public Class<?>[] loadArgs() {
        return new Class[] {LocalCacheManager.class, RemoteCacheManager.class, LocalCacheProperties.class, CacheEncoder.class, PageDetails.class, KacheLock.class};
    }
}
