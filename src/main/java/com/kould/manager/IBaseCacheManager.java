package com.kould.manager;

import com.kould.config.InterprocessCacheProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutionException;

public abstract class IBaseCacheManager {
    @Autowired
    protected InterprocessCacheManager interprocessCacheManager ;

    @Autowired
    protected RemoteCacheManager remoteCacheManager ;

    @Autowired
    protected InterprocessCacheProperties interprocessCacheProperties ;

    /**
     * 抽象层面上进行缓存的具体存储操作调控
     * 优先对远程缓存进行修改
     * @param key 缓存Key
     * @param point 切点
     * @param types 类类型
     * @return 缓存具体数据
     * @throws Throwable
     */
    public abstract Object daoWrite(String key, ProceedingJoinPoint point, String types) throws Throwable;

    /**
     * 抽象层面上进行缓存的具体读取操作调控
     * 优先从进程间缓存获取数据（在进程缓存开启的情况下）
     * @param key 缓存Key
     * @param types 类类型
     * @return 缓存具体数据
     * @throws ExecutionException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public abstract Object daoRead(String key, String types) throws Throwable;
}
