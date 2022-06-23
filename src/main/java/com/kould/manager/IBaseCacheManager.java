package com.kould.manager;

import com.kould.api.BeanLoad;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.PageDetails;
import com.kould.entity.KacheMessage;
import com.kould.properties.InterprocessCacheProperties;
import com.kould.entity.MethodPoint;

/**
 * 二级缓存管理器
 *
 * 使用异步消息队列策略实现删改逻辑时请让增删改逻辑能保证幂等性
 */
public abstract class IBaseCacheManager implements BeanLoad {

    protected InterprocessCacheManager interprocessCacheManager ;

    protected RemoteCacheManager remoteCacheManager ;

    protected InterprocessCacheProperties interprocessCacheProperties ;

    protected CacheEncoder cacheEncoder ;

    protected PageDetails<?> pageDetails;
    /**
     * 抽象层面上进行缓存的具体存储操作调控
     * 优先对远程缓存进行修改
     * @param key 缓存Key
     * @param point 切点
     * @param type 类类型
     * @return 缓存具体数据
     * @throws Exception 方法拦截时被代理方法可能产生的异常
     */
    public abstract Object daoWrite(String key, MethodPoint point, String type) throws Throwable;

    /**
     * 抽象层面上进行缓存的具体读取操作调控
     * 优先从进程间缓存获取数据（在进程缓存开启的情况下）
     * @param key 缓存Key
     * @param type 类类型
     * @return 缓存具体数据
     * @throws Exception 方法拦截时被代理方法可能产生的异常
     */
    public abstract Object daoRead(String key, String type) throws Throwable;

    public abstract void deleteCache(KacheMessage msg) throws Throwable;

    public abstract void updateCache(KacheMessage msg) throws Throwable;

    public abstract void insertCache(KacheMessage msg) throws Throwable;

    @Override
    public Class<?>[] loadArgs() {
        return new Class[] {InterprocessCacheManager.class, RemoteCacheManager.class, InterprocessCacheProperties.class, CacheEncoder.class, PageDetails.class};
    }
}
