package com.kould.aspect;

import com.kould.annotation.CacheBeanClass;
import com.kould.annotation.CacheChange;
import com.kould.annotation.ServiceCache;
import com.kould.bean.KacheConfig;
import com.kould.bean.Message;
import com.kould.encoder.CacheEncoder;
import com.kould.manager.IBaseCacheManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static com.kould.amqp.KacheQueue.*;


@Aspect
@Component
@Order(15)
public class DaoCacheAop {

    protected static ThreadLocal<Message> localVar = new ThreadLocal<>();

    @Autowired
    private KacheConfig kacheConfig ;

    @Autowired
    private IBaseCacheManager baseCacheManager;

    @Autowired
    private CacheEncoder cacheEncoder;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Pointcut(KacheConfig.POINTCUT_EXPRESSION_DAO_FIND)
    public void pointCutFind() {
    }

    @Pointcut(KacheConfig.POINTCUT_EXPRESSION_DAO_ADD)
    public void pointCutAdd() {
    }

    @Pointcut(KacheConfig.POINTCUT_EXPRESSION_DAO_REMOVE)
    public void pointCutRemove() {
    }

    @Pointcut(KacheConfig.POINTCUT_EXPRESSION_DAO_EDIT)
    public void pointCutEdit() {
    }


    @Around("@annotation(com.kould.annotation.DaoSelect) || pointCutFind()")
    public Object findArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        RLock readLock = null ;
        RLock writeLock = null;
        try {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            Method daoMethod = methodSignature.getMethod();
            String daoMethodName = daoMethod.getName() ;
            Message serviceMessage = localVar.get();
            if (serviceMessage != null && serviceMessage.getClazz().isAnnotationPresent(CacheBeanClass.class)
                    && serviceMessage.getMethod().isAnnotationPresent(ServiceCache.class)) {
                Class beanClass = serviceMessage.getCacheClazz();
                String daoArgs = cacheEncoder.argsEncode(point.getArgs());
                String lockKey = beanClass.getTypeName();
                RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);
                readLock = readWriteLock.readLock();
                readLock.lock(kacheConfig.getLockTime(), TimeUnit.SECONDS);
                String key = cacheEncoder.encode(serviceMessage.getArg()
                        , serviceMessage.getMethod(), beanClass.getName(), daoMethodName, daoArgs) ;
                Object result = baseCacheManager.get(key, cacheEncoder.getPackageClass(), beanClass);
                readLock.unlock();
                //双重检测，很酷吧 XD
                if (result == null) {
                    //为了防止缓存击穿，所以并不使用异步增加缓存，而采用同步锁限制
                    writeLock = readWriteLock.writeLock();
                    writeLock.lock(kacheConfig.getLockTime(), TimeUnit.SECONDS);
                    result = baseCacheManager.get(key, cacheEncoder.getPackageClass(), beanClass);
                    if (result == null) {
                        result = point.proceed();
                        baseCacheManager.put(key, result, beanClass);
                    }
                    writeLock.unlock();
                }
                return result;
            } else {
                return point.proceed() ;
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (readLock != null && readLock.isLocked() && readLock.isHeldByCurrentThread()) {
                readLock.unlock();
            }
            if (writeLock != null && writeLock.isLocked() && writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
        return null ;
    }

    @Around("@annotation(com.kould.annotation.DaoInsert) || @annotation(com.kould.annotation.DaoDelete) || pointCutAdd() || pointCutRemove()")
    public Object removeArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        return getObject(point,QUEUE_DELETE_CACHE,INTERPROCESS_DELETE_EXCHANGE_NAME,localVar.get()) ;
    }

    @Around("@annotation(com.kould.annotation.DaoUpdate) || pointCutEdit()")
    public Object editArroundInvoke(ProceedingJoinPoint point) throws Throwable {
        return getObject(point,QUEUE_UPDATE_CACHE,INTERPROCESS_UPDATE_EXCHANGE_NAME,localVar.get()) ;
    }

    private Object getObject(ProceedingJoinPoint point, String queue, String exchange, Message serviceMessage) throws Throwable {
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
