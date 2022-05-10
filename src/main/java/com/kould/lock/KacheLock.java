package com.kould.lock;

import com.kould.properties.DaoProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.locks.Lock;

public abstract class KacheLock {
    @Autowired
    protected DaoProperties daoProperties ;

    public abstract Lock readLock(String lockKey) ;
    public abstract Lock writeLock(String lockKey) ;
    public abstract void unLock(Lock lock) ;
    public abstract Boolean isLockedByThisThread(Lock lock) ;
}
