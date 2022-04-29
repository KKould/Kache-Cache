package com.kould.config;

public class DaoProperties {

    public DaoProperties() {
    }

    public DaoProperties(int lockTime, int baseTime, int randomTime, int poolMaxTotal, int poolMaxIdle) {
        this.lockTime = lockTime;
        this.baseTime = baseTime;
        this.randomTime = randomTime;
        this.poolMaxTotal = poolMaxTotal;
        this.poolMaxIdle = poolMaxIdle;
    }

    private int lockTime = 3 ;

    private int baseTime = 86400;

    private int randomTime = 600;

    private int poolMaxTotal = 20;

    private int poolMaxIdle = 5;

    public long getCacheTime() {
        return (long) (baseTime + Math.random() * randomTime);
    }

    public int getLockTime() {
        return lockTime;
    }

    public void setLockTime(int lockTime) {
        this.lockTime = lockTime;
    }

    public int getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(int baseTime) {
        this.baseTime = baseTime;
    }

    public int getRandomTime() {
        return randomTime;
    }

    public void setRandomTime(int randomTime) {
        this.randomTime = randomTime;
    }

    public int getPoolMaxTotal() {
        return poolMaxTotal;
    }

    public void setPoolMaxTotal(int poolMaxTotal) {
        this.poolMaxTotal = poolMaxTotal;
    }

    public int getPoolMaxIdle() {
        return poolMaxIdle;
    }

    public void setPoolMaxIdle(int poolMaxIdle) {
        this.poolMaxIdle = poolMaxIdle;
    }
}
