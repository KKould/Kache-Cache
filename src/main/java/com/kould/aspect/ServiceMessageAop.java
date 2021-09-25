package com.kould.aspect;

import com.kould.annotation.CacheBeanClass;
import com.kould.annotation.CacheChange;
import com.kould.annotation.ServiceCache;
import com.kould.bean.KacheConfig;
import com.kould.bean.Message;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(15)

public class ServiceMessageAop {

    @Autowired
    private KacheConfig kacheConfig ;

    @Pointcut(KacheConfig.POINTCUT_EXPRESSION_SERVICE)
    public void pointCut() {
    }

    @Around("@annotation(com.kould.annotation.CacheBeanClass) || pointCut()")
    public Object findArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Class targetClass = point.getTarget().getClass() ;
        Method method = null ;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        method = methodSignature.getMethod() ;
        if (!method.isAnnotationPresent(ServiceCache.class) && !method.isAnnotationPresent(CacheChange.class)) {
            return point.proceed();
        }
        Object arg = null;
        CacheBeanClass cacheBeanClass = (CacheBeanClass)targetClass.getAnnotation(CacheBeanClass.class);
        Class<?> cacheClass = cacheBeanClass.clazz();
        if (point.getArgs() != null) {

            arg = cacheClass.getDeclaredConstructor().newInstance() ;
        } else {
            arg = point.getArgs()[0] ;
        }
        DaoCacheAop.localVar.set(Message.builder()
                .arg(arg)
                .method(method)
                .clazz(targetClass)
                .cacheClazz(cacheClass)
                .build());
        return point.proceed() ;
    }
}
