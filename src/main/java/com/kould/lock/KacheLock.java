package com.kould.lock;

import java.util.concurrent.locks.Lock;

public interface KacheLock {
    Lock readLock(String lockKey) ;
    Lock writeLock(String lockKey) ;
    void unLock(Lock Lock) ;
    Boolean isLock(Lock lock) ;
}
