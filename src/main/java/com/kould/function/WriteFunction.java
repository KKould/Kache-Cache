package com.kould.function;

import org.aspectj.lang.ProceedingJoinPoint;

@FunctionalInterface
public interface WriteFunction {
    Object write(String key, ProceedingJoinPoint point, String types) throws Throwable;
}
