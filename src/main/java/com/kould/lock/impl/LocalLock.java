package com.kould.lock.impl;

import com.kould.lock.KacheLock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocalLock extends KacheLock {

    private static final LocalLock INSTANCE = new LocalLock() ;

    private static final Map<String,ReadWriteLock> readWriteLockMap = new ConcurrentHashMap<>();

    private LocalLock() {}

    public static LocalLock getInstance() {
        return INSTANCE ;
    }

    @Override
    public Lock readLock(String lockKey) {
        Lock readLock = getReadWriteLock(lockKey).readLock() ;
        readLock.lock();
        return readLock ;
    }

    @Override
    public Lock writeLock(String lockKey) {
        Lock writeLock = getReadWriteLock(lockKey).writeLock() ;
        writeLock.lock();
        return writeLock ;
    }

    @Override
    public void unLock(Lock lock) {
        lock.unlock();
    }

    @Override
    public Boolean isLockedByThisThread(Lock lock) {
        if (lock == null) {
            return false ;
        } else {
            boolean b = lock.tryLock();
            if (b) {
                lock.unlock();
            }
            return !b;
        }
    }

    private ReadWriteLock getReadWriteLock(String lockKey) {
        ReadWriteLock readWriteLock = readWriteLockMap.get(lockKey);
        if (readWriteLock == null) {
            readWriteLock = new ReentrantReadWriteLock() ;
            readWriteLockMap.put(lockKey,readWriteLock) ;
        }
        return readWriteLock;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
