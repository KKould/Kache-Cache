package com.kould.listener;

import com.kould.entity.KacheMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
维护继承CacheListener的具体实现监听器
通过该监听器处理器进行所有注册监听器的统一动作聚合
 */
public final class ListenerHandler {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private static final List<CacheListener> CACHE_LISTENER_LIST  = new CopyOnWriteArrayList<>() ;

    private ListenerHandler() {

    }

    //异步方法，不影响DaoCacheAop中的执行时间
    public static void hit(String key, String methodName, Object[] arg, String type, boolean enable) {
        if (enable) {
            executorService.execute(() -> {
                for (CacheListener cacheListener : CACHE_LISTENER_LIST) {
                    cacheListener.hit(key, KacheMessage.builder()
                            .methodName(methodName)
                            .type(type)
                            .args(arg)
                            .build()) ;
                }
            });
        }
    }

    //异步方法，不影响DaoCacheAop中的执行时间
    public static void notHit(String key, String methodName, Object[] arg, String type, boolean enable) {
        if (enable) {
            executorService.execute(() -> {
                for (CacheListener cacheListener : CACHE_LISTENER_LIST) {
                    cacheListener.notHit(key, KacheMessage.builder()
                            .methodName(methodName)
                            .type(type)
                            .args(arg)
                            .build());
                }
            });
        }
    }

    /*
    统一对外方法，用于获取调用链中各个监听器中的各个情况
     */
    public static Map<String,Object> details() {
        Map<String, Object> result = new HashMap<>();
        for (CacheListener cacheListener : CACHE_LISTENER_LIST) {
            result.put(cacheListener.getClass().getName(),cacheListener.details()) ;
        }
        return result ;
    }

    public static void register(CacheListener cacheListener) {
        CACHE_LISTENER_LIST.add(cacheListener) ;
    }
}
