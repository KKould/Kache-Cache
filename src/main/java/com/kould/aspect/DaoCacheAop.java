package com.kould.aspect;

import com.kould.annotation.ServiceCache;
import com.kould.config.KacheAutoConfig;
import com.kould.encoder.CacheEncoder;
import com.kould.handler.StrategyHandler;
import com.kould.lock.KacheLock;
import com.kould.manager.IBaseCacheManager;
import com.kould.message.KacheMessage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Aspect
@Component
@Order(15)
public class DaoCacheAop {

    private static final Logger log = LoggerFactory.getLogger(DaoCacheAop.class) ;

    //直接使用ReentrantLock而不使用接口Lock是因为需要判断锁状态
    private static final Map<String,ReentrantLock> REENTRANT_LOCK_MAP = new ConcurrentHashMap<>();

    protected static final ThreadLocal<KacheMessage> MESSAGE_THREAD_LOCAL_VAR = new ThreadLocal<>();

    @Autowired
    private KacheLock kacheLock ;

    @Autowired
    private IBaseCacheManager baseCacheManager;

    @Autowired
    private CacheEncoder cacheEncoder;

    @Autowired
    private StrategyHandler strategyHandler;

    private static final String METHOD_GET_ID = "getId" ;

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_DAO_FIND)
    public void pointCutFind() {
    }

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_DAO_ADD)
    public void pointCutAdd() {
    }

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_DAO_REMOVE)
    public void pointCutRemove() {
    }

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_DAO_EDIT)
    public void pointCutEdit() {
    }


    @Around("@annotation(com.kould.annotation.DaoSelect) || pointCutFind()")
    public Object findAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Object result = null ;
        KacheMessage serviceMessage = MESSAGE_THREAD_LOCAL_VAR.get();
        if (serviceMessage != null) {
            Lock readLock = null ;
            Lock writeLock = null;
            String key = null ;
            Class<?> beanClass = serviceMessage.getCacheClazz();
            //以PO类型进行不同持久类领域的划分，以此减少不必要的干涉开销并统一DTO的持久化操作
            String poType = beanClass.getTypeName();
            String methodStatus = serviceMessage.getMethod()
                    .getAnnotation(ServiceCache.class).status().getValue();
            String lockKey = poType + serviceMessage.getMethodName() ;
            //该PO领域的初始化
            ReentrantLock methodLock = REENTRANT_LOCK_MAP.get(lockKey);
            if (methodLock == null) {
                methodLock = new ReentrantLock();
                REENTRANT_LOCK_MAP.put(lockKey,methodLock) ;
            }
            try {
                //key拼接命名空间前缀
                key = KacheAutoConfig.CACHE_PREFIX + getKey(point, serviceMessage, beanClass, methodStatus);
                readLock = kacheLock.readLock(poType) ;
                //获取缓存
                result = baseCacheManager.get(key, beanClass);
                kacheLock.unLock(readLock);
                if (result == null) {
                    //为了防止缓存击穿，所以并不使用异步增加缓存，而采用同步锁限制
                    //使用本地锁尽可能的减少纵向（单一节点）穿透，而允许横向（分布式）穿透
                    methodLock.lock();
                    result = baseCacheManager.get(key, beanClass);
                    if (result == null) {
                        writeLock = kacheLock.writeLock(poType);
                        result = point.proceed();
                        baseCacheManager.put(key, result, beanClass);
                        kacheLock.unLock(writeLock);
                    }
                }
                //空值替换
                if (baseCacheManager.getNullValue().equals(result)) {
                    result = null ;
                }
            }catch (Exception e) {
                if (kacheLock.isLockedByThisThread(readLock)) {
                    kacheLock.unLock(readLock);
                }
                if (kacheLock.isLockedByThisThread(writeLock)) {
                    kacheLock.unLock(writeLock);
                }
                throw e ;
            } finally {
                if (methodLock.isHeldByCurrentThread() && methodLock.isLocked()) {
                    methodLock.unlock();
                }
            }
        } else {
            return point.proceed() ;
        }
        return result;
    }


    //Key获取方法
    private String getKey(ProceedingJoinPoint point, KacheMessage serviceMessage, Class<?> beanClass, String methodStatus)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        //判断serviceMethod的是否为通过id获取数据
        //  若是则直接使用id进行获取
        //  若否则经过编码后进行获取
        if (methodStatus.equals(KacheAutoConfig.SERVICE_BY_ID)) {
            //获取Service的DTO的ID
            Method methodGetId = serviceMessage.getArg().getClass().getMethod(METHOD_GET_ID, null);
            //使Key为ID
            return methodGetId.invoke(serviceMessage.getArg()).toString() ;
        }else {
            //信息摘要收集
            //获取DAO方法签名
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            Method daoMethod = methodSignature.getMethod();
            String daoMethodName = daoMethod.getName() ;
            String daoArgs = cacheEncoder.argsEncode(point.getArgs());
            //使Key为各个参数编码后的一个特殊值
            return cacheEncoder.encode(serviceMessage.getArg(), methodStatus
                    ,serviceMessage.getMethodName() , beanClass.getName(), daoMethodName, daoArgs) ;
        }
    }

    @Around("@annotation(com.kould.annotation.DaoDelete) || pointCutRemove()")
    public Object removeAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        return strategyHandler.delete(point,MESSAGE_THREAD_LOCAL_VAR.get()) ;
    }

    @Around("@annotation(com.kould.annotation.DaoInsert) || pointCutAdd()")
    public Object insertAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        return strategyHandler.insert(point,MESSAGE_THREAD_LOCAL_VAR.get()) ;
    }

    @Around("@annotation(com.kould.annotation.DaoUpdate) || pointCutEdit()")
    public Object editAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        return strategyHandler.update(point,MESSAGE_THREAD_LOCAL_VAR.get()) ;
    }

}
