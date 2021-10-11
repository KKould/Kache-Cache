package com.kould.lock;

import com.kould.bean.KacheConfig;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Component
public class RedissonLock implements KacheLock{

    @Autowired
    private KacheConfig kacheConfig ;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Lock readLock(String lockKey) {
        RLock readLock = null ;
        RReadWriteLock rReadWriteLock = redissonClient.getReadWriteLock(lockKey);
        readLock = rReadWriteLock.readLock() ;
        readLock.lock(kacheConfig.getLockTime(), TimeUnit.SECONDS);
        return readLock ;
    }

    @Override
    public Lock writeLock(String lockKey) {
        RLock writeLock = null ;
        RReadWriteLock rReadWriteLock = redissonClient.getReadWriteLock(lockKey) ;
        writeLock = rReadWriteLock.writeLock() ;
        writeLock.lock(kacheConfig.getLockTime(), TimeUnit.SECONDS);
        return writeLock ;
    }

    @Override
    public void unLock(Lock lock) {
        RLock rLock = (RLock) lock ;
        rLock.unlock();
    }

    @Override
    public Boolean isLock(Lock lock) {
        RLock rLock = (RLock) lock ;
        if (rLock != null && rLock.isLocked() && rLock.isHeldByCurrentThread()) {
            return false ;
        } else {
            return true ;
        }
    }
}
