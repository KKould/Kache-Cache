package com.kould.listener.impl;

import java.io.Serializable;
import java.util.Map;

public class StatisticSnapshot implements Serializable {
    public StatisticSnapshot(Map<String, MethodStatistic> STATISTIC_MAP, long SUM_HIT, long SUM_NOT_HIT) {
        this.STATISTIC_MAP = STATISTIC_MAP;
        this.SUM_HIT = SUM_HIT;
        this.SUM_NOT_HIT = SUM_NOT_HIT;
    }

    private final Map<String, MethodStatistic> STATISTIC_MAP;

    private final long SUM_HIT;

    private final long SUM_NOT_HIT;
}
