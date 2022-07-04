package com.kould.lock.impl;

import com.kould.function.Function;
import com.kould.lock.KacheLock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalLock extends KacheLock {

    private static final Map<String, Lock> LOCK_MAP = new HashMap<>();

    @Override
    public <T, R> R syncFunction(String lockKey, Function<T, R> function, T arg) throws Exception {
        R result;
        Lock lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new ReentrantLock());
        lock.lock();
        try {
            result = function.apply(arg);
        } finally {
            lock.unlock();
        }
        return result;
    }

    @Override
    public <T, R> R trySyncFunction(String lockKey, Function<T, R> function, T arg, long time) throws Exception {
        R result;
        Lock lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new ReentrantLock());
        if (lock.tryLock(time, TimeUnit.MILLISECONDS)) {
            try {
                result = function.apply(arg);
            } finally {
                lock.unlock();
            }
        } else {
            result =  function.apply(arg);
        }
        return result;
    }
}
