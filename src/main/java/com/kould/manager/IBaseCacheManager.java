package com.kould.manager;

import java.util.concurrent.ExecutionException;

public interface IBaseCacheManager {
    <T> T put(String key, T result, Class<?> resultClass) throws ExecutionException;
    Object get(String key, Class<?> resultClass) throws ExecutionException;
}
