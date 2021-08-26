package com.kould.aspect;

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

    @Around("@annotation(com.kould.annotation.ServiceCache) || pointCut()")
    public Object findArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Object arg = null;
        Method method = null ;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        method = methodSignature.getMethod() ;
        DaoCacheAop.localVar.set(Message.builder()
                .arg(arg)
                .method(method)
                .clazz(point.getTarget().getClass())
                .build());
        return point.proceed() ;
    }
}
