package com.kould.lock.impl;

import com.kould.lock.KacheLock;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class RedissonLock extends KacheLock {

    private static final RedissonLock INSTANCE = new RedissonLock() ;

    @Autowired
    private RedissonClient redissonClient;

    private RedissonLock() {}

    public static RedissonLock getInstance() {
        return INSTANCE ;
    }

    @Override
    public Lock readLock(String lockKey) {
        RLock readLock = null ;
        RReadWriteLock rReadWriteLock = redissonClient.getReadWriteLock(lockKey);
        readLock = rReadWriteLock.readLock() ;
        readLock.lock(daoProperties.getLockTime(), TimeUnit.SECONDS);
        return readLock ;
    }

    @Override
    public Lock writeLock(String lockKey) {
        RLock writeLock = null ;
        RReadWriteLock rReadWriteLock = redissonClient.getReadWriteLock(lockKey) ;
        writeLock = rReadWriteLock.writeLock() ;
        writeLock.lock(daoProperties.getLockTime(), TimeUnit.SECONDS);
        return writeLock ;
    }

    @Override
    public void unLock(Lock lock) {
        RLock rLock = (RLock) lock ;
        rLock.unlock();
    }

    @Override
    public Boolean isLockedByThisThread(Lock lock) {
        if (lock == null) {
            return false ;
        }
        RLock rLock = (RLock) lock ;
        return rLock.isLocked() && rLock.isHeldByCurrentThread();
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
