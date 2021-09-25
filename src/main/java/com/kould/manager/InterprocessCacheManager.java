package com.kould.manager;

import java.util.concurrent.ExecutionException;

public interface InterprocessCacheManager {
    <T> T update(String key,T result, Class<?> resultClass) ;
    Object get(String key, Class<?> resultClass) throws ExecutionException;
    void clear(Class<?> resultClass) throws ExecutionException;
    <T> T put(String key, T result, Class<?> resultClass) throws ExecutionException;
}
