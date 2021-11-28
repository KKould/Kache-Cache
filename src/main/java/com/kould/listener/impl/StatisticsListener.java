package com.kould.listener.impl;

import com.kould.listener.CacheListener;
import com.kould.message.KacheMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public class StatisticsListener extends CacheListener {

    private static final Map<String, MethodStatistic> STATISTIC_MAP = new HashMap<>() ;

    private static final LongAdder SUM_HIT = new LongAdder();

    private static final LongAdder SUM_NOT_HIT = new LongAdder();

    @Override
    public void hit(String key, KacheMessage msg) {
        MethodStatistic methodStatistic = getStatistic(key, msg);
        methodStatistic.hitIncrement();
        SUM_HIT.increment(); ;
    }

    @Override
    public void notHit(String key,KacheMessage msg) {
        MethodStatistic methodStatistic = getStatistic(key, msg);
        methodStatistic.noHitIncrement();
        SUM_NOT_HIT.increment();
    }

    private MethodStatistic getStatistic(String key, KacheMessage msg) {
        return STATISTIC_MAP.computeIfAbsent(msg.getMethodName(),k -> new MethodStatistic(key)) ;
    }

    @Override
    public Object details() {
        return new StatisticSnapshot(STATISTIC_MAP,SUM_HIT.longValue(),SUM_NOT_HIT.longValue());
    }
}
