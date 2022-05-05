package com.kould.listener.impl;

import com.kould.listener.CacheListener;
import com.kould.listener.ListenerHandler;
import com.kould.entity.KacheMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public class StatisticsListener extends CacheListener {

    private StatisticsListener() {}

    public static StatisticsListener newInstance() {
        StatisticsListener statisticsListener = new StatisticsListener();
        statisticsListener.register();
        return statisticsListener ;
    }

    @Override
    protected void register() {
        ListenerHandler.register(this);
    }

    private static final Map<String, MethodStatistic> STATISTIC_MAP = new HashMap<>() ;

    private static final LongAdder SUM_HIT = new LongAdder();

    private static final LongAdder SUM_NOT_HIT = new LongAdder();

    @Override
    public void hit(String key, KacheMessage msg) {
        MethodStatistic methodStatistic = getMethodStatistic(key, msg);
        methodStatistic.hitIncrement();
        SUM_HIT.increment(); ;
    }

    @Override
    public void notHit(String key, KacheMessage msg) {
        MethodStatistic methodStatistic = getMethodStatistic(key, msg);
        methodStatistic.noHitIncrement();
        SUM_NOT_HIT.increment();
    }

    @Override
    public Object details() {
        return new StatisticSnapshot(STATISTIC_MAP,SUM_HIT.longValue(),SUM_NOT_HIT.longValue());
    }

    private MethodStatistic getMethodStatistic(String key, KacheMessage msg) {
        String fullName = msg.getTypes() + "."+ msg.getMethodName();
        MethodStatistic methodStatistic = STATISTIC_MAP.computeIfAbsent(fullName, k -> new MethodStatistic());
        methodStatistic.getKey_set().add(key);
        return methodStatistic;
    }
}
