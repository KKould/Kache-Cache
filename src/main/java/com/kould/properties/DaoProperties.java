package com.kould.properties;

public class DaoProperties {

    public DaoProperties() {
    }

    public DaoProperties(int baseTime, int randomTime, int poolMaxTotal, int poolMaxIdle, long casKeepTIme) {
        this.baseTime = baseTime;
        this.randomTime = randomTime;
        this.poolMaxTotal = poolMaxTotal;
        this.poolMaxIdle = poolMaxIdle;
        this.casKeepTime = casKeepTIme;
    }

    private int baseTime = 86400;

    private int randomTime = 600;

    private int poolMaxTotal = 20;

    private int poolMaxIdle = 5;

    private long casKeepTime = 1L;

    public long getCacheTime() {
        return (long) (baseTime + Math.random() * randomTime);
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

    public long getCasKeepTime() {
        return casKeepTime;
    }

    public void setCasKeepTime(long casKeepTIme) {
        this.casKeepTime = casKeepTIme;
    }
}
