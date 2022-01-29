package com.kould.function;

import org.aspectj.lang.ProceedingJoinPoint;

@FunctionalInterface
public interface WriteFunction {
    Object write(String key, Class<?> beanClass, String lockKey, ProceedingJoinPoint point) throws Throwable;
}
