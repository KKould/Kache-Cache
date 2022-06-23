package com.kould.function;

import io.lettuce.core.api.sync.RedisCommands;

@FunctionalInterface
public interface SyncCommandCallback<T> {
    /**
     * Redis操作函数
     * @param commands redis同步命令客户端
     * @return 返回
     * @throws Exception 操作时潜在的错误
     */
    T doInConnection(RedisCommands<String, Object> commands) throws Throwable;
}
