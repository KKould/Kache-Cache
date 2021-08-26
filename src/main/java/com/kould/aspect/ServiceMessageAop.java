package com.kould.aspect;

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
import org.springframework.util.StringUtils;

@Aspect
@Component
@Order(15)

public class ServiceMessageAop {

    @Autowired
    private KacheConfig kacheConfig ;

    @Pointcut(KacheConfig.POINTCUT_EXPRESSION_SERVICE)
    public void pointCut() {
    }

    @Around("@annotation(com.kould.annotation.ServiceCache) || pointCut()")
    public Object findArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Object arg = null;
        String methodName = null ;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        ServiceCache serviceCache = (ServiceCache) methodSignature.getMethod().getAnnotation(ServiceCache.class);
        if (serviceCache != null && !StringUtils.isEmpty(serviceCache.methodName())) {
            methodName = serviceCache.methodName() ;
        } else {
            methodName = point.getSignature().getName();
        }
        //判断是否为无参方法
        if (point.getArgs() != null && point.getArgs().length > 0) {
            arg = point.getArgs()[0] ;
        } else {
            methodName = methodName + kacheConfig.getMethodNoArgTag() ;
        }
        DaoCacheAop.localVar.set(Message.builder()
                .arg(arg)
                .method(methodName)
                .clazz(point.getTarget().getClass())
                .build());
        return point.proceed() ;
    }
}
