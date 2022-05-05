package com.kould.function;

import com.kould.config.Status;
import com.kould.proxy.MethodPoint;

import java.lang.reflect.Method;

@FunctionalInterface
public interface KeyFunction {
    String encode(MethodPoint point, String methodName, Method method , Object args, String types, Status methodStatus) ;
}
