package com.kould.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocalLock implements KacheLock{

    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock() ;

    @Override
    public Lock readLock(String lockKey) {
        Lock readLock = readWriteLock.readLock() ;
        readLock.lock();
        return readLock ;
    }

    @Override
    public Lock writeLock(String lockKey) {
        Lock writeLock = readWriteLock.writeLock() ;
        writeLock.lock();
        return writeLock ;
    }

    @Override
    public void unLock(Lock Lock) {
        Lock.unlock();
    }

    @Override
    public Boolean isLock(Lock lock) {
        return lock == null;
    }
}
