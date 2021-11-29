package com.kould.listener.impl;

import java.io.Serializable;
import java.util.Map;

public class StatisticSnapshot implements Serializable {
    public StatisticSnapshot() {
    }

    public StatisticSnapshot(Map<String, MethodStatistic> statisticMap, long sumHit, long sumNotHit) {
        this.statisticMap = statisticMap;
        this.sumHit = sumHit;
        this.sumNotHit = sumNotHit;
    }

    private Map<String, MethodStatistic> statisticMap;

    private long sumHit;

    private long sumNotHit;

    public Map<String, MethodStatistic> getStatisticMap() {
        return statisticMap;
    }

    public void setStatisticMap(Map<String, MethodStatistic> statisticMap) {
        this.statisticMap = statisticMap;
    }

    public long getSumHit() {
        return sumHit;
    }

    public void setSumHit(long sumHit) {
        this.sumHit = sumHit;
    }

    public long getSumNotHit() {
        return sumNotHit;
    }

    public void setSumNotHit(long sumNotHit) {
        this.sumNotHit = sumNotHit;
    }
}
