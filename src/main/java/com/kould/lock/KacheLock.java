package com.kould.lock;

import com.kould.function.Function;

public abstract class KacheLock {
    public abstract <T, R> R syncFunction(String lockKey, Function<T, R> function, T arg) throws Exception;
    public abstract <T, R> R trySyncFunction(String lockKey, Function<T, R> function, T arg, long time) throws Exception;
}