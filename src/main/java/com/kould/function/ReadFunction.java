package com.kould.function;

@FunctionalInterface
public interface ReadFunction {
    Object read(String key, String types) throws Throwable;
}
