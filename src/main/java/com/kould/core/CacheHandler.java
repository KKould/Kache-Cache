package com.kould.core;

import com.kould.function.KeyFunction;
import com.kould.function.ReadFunction;
import com.kould.function.WriteFunction;
import org.aspectj.lang.ProceedingJoinPoint;

public abstract class CacheHandler {

    public abstract Object load(ProceedingJoinPoint point, boolean listenerEnable
            , ReadFunction readFunction, WriteFunction writeFunction, KeyFunction keyFunction , String types) throws Throwable ;
}
