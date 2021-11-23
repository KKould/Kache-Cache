package com.kould.aspect;

import com.kould.annotation.CacheBeanClass;
import com.kould.annotation.CacheChange;
import com.kould.annotation.ServiceCache;
import com.kould.config.KacheAutoConfig;
import com.kould.encoder.CacheEncoder;
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
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.kould.amqp.KacheQueue.*;


@Aspect
@Component
@Order(15)
public class DaoCacheAop {

    private static final Logger log = LoggerFactory.getLogger(DaoCacheAop.class) ;

    //直接使用ReentrantLock而不使用接口Lock是因为需要判断锁状态
    private static final Map<String,ReentrantLock> lockMap = new ConcurrentHashMap<>();

    protected static final ThreadLocal<KacheMessage> localVar = new ThreadLocal<>();

    @Autowired
    private KacheLock kacheLock ;

    @Autowired
    private IBaseCacheManager baseCacheManager;

    @Autowired
    private CacheEncoder cacheEncoder;

    @Autowired
    private AmqpTemplate amqpTemplate;

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
    public Object findArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Object result = null ;
        KacheMessage serviceMessage = localVar.get();
        if (serviceMessage != null && serviceMessage.getClazz().isAnnotationPresent(CacheBeanClass.class)
                && serviceMessage.getMethod().isAnnotationPresent(ServiceCache.class)) {
            //局部变量初始化与局部引用声明
            Lock readLock = null ;
            Lock writeLock = null;
            String key = null ;
            Class<?> beanClass = serviceMessage.getCacheClazz();
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            Method daoMethod = methodSignature.getMethod();
            String daoMethodName = daoMethod.getName() ;
            String daoArgs = cacheEncoder.argsEncode(point.getArgs());
            //以PO类型进行不同持久类领域的划分，以此减少不必要的干涉开销并统一DTO的持久化操作
            String poType = beanClass.getTypeName();
            String methodStatus = serviceMessage.getMethod()
                    .getAnnotation(ServiceCache.class).status().getValue();
            String lockKey = poType + serviceMessage.getMethodName() ;
            //该PO领域的初始化
            ReentrantLock typeLock = lockMap.get(lockKey);
            if (typeLock == null) {
                typeLock = new ReentrantLock();
                lockMap.put(lockKey,typeLock) ;
            }
            try {
                //判断serviceMethod的是否为通过id获取数据
                //  若是则直接使用id进行获取
                //  若否则经过编码后进行获取
                if (methodStatus.equals(KacheAutoConfig.SERVICE_BY_ID)) {
                    Method methodGetId = serviceMessage.getArg().getClass().getMethod(METHOD_GET_ID, null);
                    key = methodGetId.invoke(serviceMessage.getArg()).toString() ;
                }else {
                    key = cacheEncoder.encode(serviceMessage.getArg(), methodStatus
                            ,serviceMessage.getMethodName() , beanClass.getName(), daoMethodName, daoArgs) ;
                }
                readLock = kacheLock.readLock(poType) ;
                //获取缓存
                result = baseCacheManager.get(key, beanClass);
                kacheLock.unLock(readLock);
                if (result == null) {
                    //为了防止缓存击穿，所以并不使用异步增加缓存，而采用同步锁限制
                    //使用本地锁尽可能的减少纵向（单一节点）穿透，而允许横向（分布式）穿透
                    typeLock.lock();
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
                if (kacheLock.isLock(readLock)) {
                    kacheLock.unLock(readLock);
                }
                if (kacheLock.isLock(writeLock)) {
                    kacheLock.unLock(writeLock);
                }
                throw e ;
            } finally {
                if (typeLock.isLocked()) {
                    typeLock.unlock();
                }
            }
        } else {
            return point.proceed() ;
        }
        return result;
    }

    @Around("@annotation(com.kould.annotation.DaoInsert) || @annotation(com.kould.annotation.DaoDelete) || pointCutAdd() || pointCutRemove()")
    public Object removeArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        return getObject(point,QUEUE_DELETE_CACHE,INTERPROCESS_DELETE_EXCHANGE_NAME,localVar.get()) ;
    }

    @Around("@annotation(com.kould.annotation.DaoUpdate) || pointCutEdit()")
    public Object editArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        return getObject(point,QUEUE_UPDATE_CACHE,INTERPROCESS_UPDATE_EXCHANGE_NAME,localVar.get()) ;
    }

    private Object getObject(ProceedingJoinPoint point, String queue, String exchange, KacheMessage serviceMessage) throws Throwable {
        //先通过植入点的方法执行后查看是否会发生错误，以免误操作
        Object proceed = point.proceed();
        if (serviceMessage != null && serviceMessage.getMethod().isAnnotationPresent(CacheChange.class)) {
            serviceMessage.setMethod(null);
            this.amqpTemplate.convertAndSend(queue, serviceMessage);
            this.amqpTemplate.convertAndSend(exchange, "", serviceMessage);
        }
        return proceed ;
    }

}
