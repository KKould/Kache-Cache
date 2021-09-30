package com.kould.manager;

import java.util.concurrent.ExecutionException;

public interface InterprocessCacheManager {
    <T> T update(String key,T result, Class<?> beanClass) ;
    Object get(String key, Class<?> beanClass) throws ExecutionException;
    void clear(Class<?> beanClass) throws ExecutionException;
    <T> T put(String key, T result, Class<?> beanClass) throws ExecutionException;
}
