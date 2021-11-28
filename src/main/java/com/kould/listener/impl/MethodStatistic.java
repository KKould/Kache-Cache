package com.kould.listener.impl;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.LongAdder;

public class MethodStatistic implements Serializable {

    private static final Set<String> KEY_SET = new CopyOnWriteArraySet<>();

    private static final LongAdder HIT = new LongAdder();

    private static final LongAdder NOT_HIT = new LongAdder();

    public MethodStatistic(String key) {
        KEY_SET.add(key) ;
    }

    public void hitIncrement() {
        HIT.increment(); ;
    }

    public void noHitIncrement() {
        NOT_HIT.increment(); ;
    }
}
