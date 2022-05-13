package com.kould.strategy;

import com.kould.manager.IBaseCacheManager;

/**
 * 异步更新处理器
 * 分布式处理需要通过广播实现各节点消息同时消费，且需保持增删改幂等性，防止重复删除
 */
public abstract class ASyncStrategy extends Strategy {

    protected ASyncStrategy(IBaseCacheManager baseCacheManager) {
        super(baseCacheManager);
    }
}
