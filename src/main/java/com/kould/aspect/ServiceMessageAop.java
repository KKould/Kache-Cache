package com.kould.aspect;

import com.kould.annotation.CacheBeanClass;
import com.kould.annotation.CacheChange;
import com.kould.annotation.ServiceCache;
import com.kould.config.KacheAutoConfig;
import com.kould.message.KacheMessage;
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

//Service数据采集AOP
public final class ServiceMessageAop {

    @Autowired
    private KacheAutoConfig kacheAutoConfig;

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_SERVICE)
    public void pointCut() {
    }

    /**
     * 为DAO层缓存AOP提供以下数据
     *  1、该织入点方法
     *  2、该织入点方法内参数（首位）
     *  3、目标对象的类
     *  4、缓存PO的类
     *  以ThreadLocal的形式传入给DAO层
     * @param point 切入点
     * @return point执行，不影响其正常工作
     * @throws Throwable
     */
    @Around("@annotation(com.kould.annotation.CacheBeanClass) || pointCut()")
    public Object findArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Class<?> targetClass = point.getTarget().getClass() ;
        Method method = null ;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        method = methodSignature.getMethod() ;
        //注解检测
        if (method.isAnnotationPresent(ServiceCache.class) || method.isAnnotationPresent(CacheChange.class)) {
            CacheBeanClass cacheBeanClass = targetClass.getAnnotation(CacheBeanClass.class);
            Class<?> cacheClass = cacheBeanClass.clazz();
            //传递Service方法签名
            DaoCacheAop.MESSAGE_THREAD_LOCAL_VAR.set(KacheMessage.builder()
                    .method(method)
                    .clazz(targetClass)
                    .cacheClazz(cacheClass)
                    .methodName(method.getName())
                    .build());
            return point.proceed();
        } else {
            return point.proceed() ;
        }
    }
}
