package com.kould.function;

public interface Function<T, R> {
    R apply(T t) throws Exception;
}
