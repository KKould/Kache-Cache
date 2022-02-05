package com.kould.function;

import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

@FunctionalInterface
public interface KeyFunction {
    Object encode(ProceedingJoinPoint point, String methodName, Method method , Object args, String types) ;
}
