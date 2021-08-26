package com.kould.manager;

public interface IBaseCacheManager {
    <T> T put(String key, T result) ;
    Object get(String key, Class<?> resultClass) ;
}
