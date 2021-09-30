package com.kould.manager;

import java.util.concurrent.ExecutionException;

public interface IBaseCacheManager {
    <T> T put(String key, T result, Class<?> beanClass) throws ExecutionException;
    Object get(String key, Class<?> resultClass, Class<?> beanClass) throws ExecutionException;
}
