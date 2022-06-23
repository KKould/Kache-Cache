package com.kould.core.impl;

import com.kould.api.Kache;
import com.kould.core.CacheHandler;
import com.kould.entity.Status;
import com.kould.entity.NullValue;
import com.kould.exception.KacheAsyncWriteException;
import com.kould.listener.ListenerHandler;
import com.kould.entity.MethodPoint;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BaseCacheHandler extends CacheHandler {

    private static final Map<String, CompletableFuture<?>> FUTURE_INDEX = new ConcurrentHashMap<>();

    @Override
    public Object load(MethodPoint point, String types, Status methodStatus) throws Throwable {
        boolean enable = this.listenerProperties.isEnable();
        Method daoMethod = point.getMethod();
        String methodName = daoMethod.getName() ;
        //该PO领域的初始化
        //需要使用types表示其所涉及的缓存Bean的Class
        String key = this.encoder.getDaoKey(point, methodName, types, methodStatus);
        //对types进行处理,获取其主缓存Bean的Class名传入
        String mainType = types.split(Kache.SPLIT_TAG, 2)[0];
        //key拼接命名空间前缀
        Object[] daoArgs = point.getArgs();
        //以PO类型进行不同持久类领域的划分并拼接参数与方法作为幂等凭据
        String lockKey = (mainType + methodName + Arrays.hashCode(daoArgs));
        //获取缓存
        Object result = this.baseCacheManager.daoRead(key , mainType);
        if (result == null) {
            // 通过新建Future进行异步写入,FUTURE_INDEX保证线程安全避免重复写入
            // 避免纵向（单一节点）击穿,而允许横向（分布式）击穿
            CompletableFuture<?> completableFuture = FUTURE_INDEX.computeIfAbsent(lockKey, k -> CompletableFuture.supplyAsync(() -> {
                try {
                    ListenerHandler.notHit(key, methodName, daoArgs, mainType, enable);
                    return this.baseCacheManager.daoWrite(key , point, mainType);
                } catch (Throwable e) {
                    throw new KacheAsyncWriteException(e.getMessage(),e);
                }
            }));
            // 堵塞读取,聚集该击穿至写入时间内的线程统一获取数据并解除堵塞
            result = completableFuture.get();
            // 该次击穿结束，移除该Future帧避免下次击穿获取脏值
            FUTURE_INDEX.remove(lockKey);
        } else {
            ListenerHandler.hit(key, methodName, daoArgs, mainType, enable);
        }
        //空值替换
        if (result instanceof NullValue) {
            result = null ;
        }
        return result;
    }
}
