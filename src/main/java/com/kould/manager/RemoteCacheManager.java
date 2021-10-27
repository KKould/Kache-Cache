package com.kould.manager;

import java.util.List;

public interface RemoteCacheManager {
    boolean hasKey(String key) ;
    <T> T put(String key, T result) ;
    List<String> keys(String pattern) ;
    Long del(String... keys) ;
    <T> T updateById(String id,T result) ;
    Object get(String key, Class<?> beanClass) ;
}
