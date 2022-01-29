package com.kould.core.impl;

import com.kould.config.KacheAutoConfig;
import com.kould.core.CacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.function.KeyFunction;
import com.kould.function.ReadFunction;
import com.kould.function.WriteFunction;
import com.kould.listener.ListenerHandler;
import com.kould.manager.IBaseCacheManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class BaseCacheHandler extends CacheHandler {

    private static final Logger log = LoggerFactory.getLogger(BaseCacheHandler.class) ;

    //直接使用ReentrantLock而不使用接口Lock是因为需要判断锁状态
    private static final Map<String,ReentrantLock> REENTRANT_LOCK_MAP = new ConcurrentHashMap<>();

    @Autowired
    private IBaseCacheManager baseCacheManager;

    @Autowired
    private CacheEncoder cacheEncoder;

    @Override
    public Object load(ProceedingJoinPoint point, Class<?> beanClass, boolean listenerEnable, ReadFunction readFunction, WriteFunction writeFunction, KeyFunction keyFunction ,String types) throws Throwable {
        Object result;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method daoMethod = methodSignature.getMethod();
        String methodName = daoMethod.getName() ;
        //以PO类型进行不同持久类领域的划分
        String poType = beanClass.getTypeName();
        String lockKey = poType + methodName ;
        //该PO领域的初始化
        //通过lambda表达式延迟加载锁并获取
        ReentrantLock methodLock = REENTRANT_LOCK_MAP.computeIfAbsent(lockKey, k -> new ReentrantLock()) ;
        try {
            //key拼接命名空间前缀
            Object daoArgs = point.getArgs();
            String key = KacheAutoConfig.CACHE_PREFIX + keyFunction.encode(point, beanClass, methodName, daoMethod, daoArgs, types);
            //获取缓存
            result = readFunction.read(key, beanClass ,lockKey);
            if (result == null) {
                //为了防止缓存击穿，所以并不使用异步增加缓存，而采用同步锁限制
                //使用本地锁尽可能的减少纵向（单一节点）穿透，而允许横向（分布式）穿透
                methodLock.lock();
                result = readFunction.read(key, beanClass ,lockKey);
                if (result == null) {
                    //此处为真正未命中处，若置于上层则可能导致缓存穿透的线程一起被计数而导致不够准确
                    ListenerHandler.notHit(key,beanClass, methodName, daoArgs, listenerEnable);
                    result = writeFunction.write(key, beanClass ,lockKey, point);
                } else {
                    //将同步后获取缓存的线程的命中也计数
                    ListenerHandler.hit(key, beanClass, methodName, daoArgs, listenerEnable);
                }
            } else {
                ListenerHandler.hit(key, beanClass, methodName, daoArgs, listenerEnable);
            }
            //空值替换
            if (baseCacheManager.getNullValue().equals(result)) {
                result = null ;
            }
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            throw e ;
        } finally {
            if (methodLock.isHeldByCurrentThread() && methodLock.isLocked()) {
                methodLock.unlock();
            }
        }
        return result;
    }
}
