package com.kould.manager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface RemoteCacheManager {
    boolean hasKey(String key) ;
    <T> T put(String key, T result) ;
    List<String> keys(String pattern) ;
    Long del(String... keys) ;
    <T> T updateById(String id,T result) ;
    Object get(String key, Class<?> resultClass, Class<?> beanClass) ;
}
