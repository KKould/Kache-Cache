package com.kould.lock;

import com.kould.config.DaoProperties;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class RedissonLock implements KacheLock{

    @Autowired
    private DaoProperties daoProperties ;

    @Autowired
    private RedissonClient redissonClient;

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
    public Boolean isLock(Lock lock) {
        if (lock == null) {
            return true ;
        }
        RLock rLock = (RLock) lock ;
        return !rLock.isLocked() || !rLock.isHeldByCurrentThread();
    }
}
