package com.kould.manager;

import java.util.List;

public interface InterprocessCacheManager {
    boolean hasKey(String key) ;
    <T> T update(String key,T result) ;
    Object get(String key) ;
    void clear() ;
    <T> T put(String key, T result, int limit) ;
}
