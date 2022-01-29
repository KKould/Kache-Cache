package com.kould.function;

import java.util.concurrent.ExecutionException;

@FunctionalInterface
public interface ReadFunction {
    Object read(String key, Class<?> beanClass, String lockKey) throws IllegalAccessException, ExecutionException, NoSuchFieldException;
}
