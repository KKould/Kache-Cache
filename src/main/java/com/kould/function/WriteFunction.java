package com.kould.function;

import com.kould.entity.MethodPoint;

@FunctionalInterface
public interface WriteFunction {
    Object write(String key, MethodPoint point, String types) throws Exception;
}
