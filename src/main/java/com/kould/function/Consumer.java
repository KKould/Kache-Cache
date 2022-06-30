package com.kould.function;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t) throws Exception;
}
