package com.kould.function;

import com.kould.proxy.MethodPoint;

@FunctionalInterface
public interface WriteFunction {
    Object write(String key, MethodPoint point, String types) throws Exception;
}
