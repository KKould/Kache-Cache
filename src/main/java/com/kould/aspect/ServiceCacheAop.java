package com.kould.aspect;

import com.kould.annotation.MethodClass;
import com.kould.annotation.ServiceClass;
import com.kould.config.KacheAutoConfig;
import com.kould.config.ListenerProperties;
import com.kould.core.CacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.manager.IBaseCacheManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

@Aspect
@Order(15)
//Service数据采集AOP
public class ServiceCacheAop {

    @Autowired
    private CacheHandler cacheHandler;

    @Autowired
    private ListenerProperties listenerProperties;

    @Autowired
    private IBaseCacheManager baseCacheManager;

    @Autowired
    private CacheEncoder cacheEncoder;

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_SERVICE_MYBATIS_PLUS_ISERVICE)
    public void pointCutIService() {
    }

    @Around("@within(com.kould.annotation.ServiceClass) || @within(com.kould.annotation.MethodClass) || pointCutIService()")
    public Object AroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Class<?> targetClass = point.getTarget().getClass();
        String classKey = null;
        Class<?> beanClass = null;
        MethodClass methodClass = targetClass.getAnnotation(MethodClass.class);
        ServiceClass serviceClass = targetClass.getAnnotation(ServiceClass.class);
        if (serviceClass != null) {
            beanClass = serviceClass.value();
        }
        if (methodClass != null) {
            StringBuilder sb = new StringBuilder();
            Class<?>[] value = methodClass.value();
            for (Class<?> clazz : value) {
                sb.append(clazz.getTypeName());
            }
            classKey = sb.toString();
        } else if (beanClass != null){
            classKey = serviceClass.value().getTypeName();
        }
        if (classKey != null) {
            return cacheHandler.load(point, listenerProperties.isEnable(), baseCacheManager::serviceRead
                    , baseCacheManager::serviceWrite, cacheEncoder::getServiceKey, classKey);
        } else {
            return point.proceed();
        }
    }
}
