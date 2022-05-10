package com.kould.function;

import com.kould.entity.Status;
import com.kould.proxy.MethodPoint;

@FunctionalInterface
public interface KeyFunction {
    String encode(MethodPoint point, String methodName, String types, Status methodStatus) ;
}
