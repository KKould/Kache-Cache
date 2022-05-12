package com.kould.core;

import com.kould.entity.Status;
import com.kould.function.KeyFunction;
import com.kould.function.ReadFunction;
import com.kould.function.WriteFunction;
import com.kould.entity.MethodPoint;

public abstract class CacheHandler {

    public abstract Object load(MethodPoint point, boolean listenerEnable, ReadFunction readFunction
            , WriteFunction writeFunction, KeyFunction keyFunction , String types, Status status) throws Exception ;
}
