package com.kould.aspect;

import com.kould.annotation.CacheBean;
import com.kould.config.KacheAutoConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Order(15)
//Service数据采集AOP
public class ServiceClassAop {

    private static final Map<String, Class<?>> CLASS_MAP = new ConcurrentHashMap<>() ;

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_SERVICE_MYBATIS_PLUS_ISERVICE)
    public void pointCutIService() {
    }

    @Around("@within(com.kould.annotation.CacheBean) || @within(com.kould.annotation.CacheImpl) || pointCutIService()")
    public Object AroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Class<?> targetClass = point.getTarget().getClass() ;
        String key = targetClass.getName() ;
        Class<?> cacheClass = CLASS_MAP.get(key) ;
        if (cacheClass == null) {
            CacheBean cacheBean = targetClass.getAnnotation(CacheBean.class);
            if (cacheBean == null) {
                throw new Exception("未标注CacheBeanClass或注解失效") ;
            }
            cacheClass = CLASS_MAP.computeIfAbsent(key, k -> cacheBean.clazz()) ;
        }
        DaoCacheAop.CLASS_THREAD_LOCAL.set(cacheClass);
        return point.proceed();
    }
}
