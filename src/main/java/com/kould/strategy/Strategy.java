package com.kould.strategy;

import com.kould.manager.IBaseCacheManager;
import com.kould.entity.KacheMessage;
import com.kould.entity.MethodPoint;

//负责缓存删除/更新处理与缓存存活时间策略
public abstract class Strategy {

    public Strategy(IBaseCacheManager baseCacheManager) {
        this.baseCacheManager = baseCacheManager;
    }

    protected IBaseCacheManager baseCacheManager;

    public abstract Object delete(MethodPoint point, KacheMessage serviceMessage) throws Exception;
    public abstract Object update(MethodPoint point, KacheMessage serviceMessage) throws Exception;
    public abstract Object insert(MethodPoint point, KacheMessage serviceMessage) throws Exception;
}
