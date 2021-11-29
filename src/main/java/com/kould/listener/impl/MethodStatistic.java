package com.kould.listener.impl;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.LongAdder;

public class MethodStatistic implements Serializable {

    private Set<String> key_set = new CopyOnWriteArraySet<>();

    private LongAdder hit = new LongAdder();

    private LongAdder notHit = new LongAdder();

    public void hitIncrement() {
        hit.increment();
    }

    public void noHitIncrement() {
        notHit.increment();
    }

    public Set<String> getKey_set() {
        return key_set;
    }

    public void setKey_set(Set<String> key_set) {
        this.key_set = key_set;
    }

    public LongAdder getHit() {
        return hit;
    }

    public void setHit(LongAdder hit) {
        this.hit = hit;
    }

    public LongAdder getNotHit() {
        return notHit;
    }

    public void setNotHit(LongAdder notHit) {
        this.notHit = notHit;
    }


}
