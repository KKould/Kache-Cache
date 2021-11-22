package com.kould.aspect;

import com.kould.annotation.CacheBeanClass;
import com.kould.annotation.CacheChange;
import com.kould.annotation.ServiceCache;
import com.kould.config.KacheAutoConfig;
import com.kould.message.Message;
import com.kould.encoder.CacheEncoder;
import com.kould.lock.KacheLock;
import com.kould.manager.IBaseCacheManager;
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

    private static final Map<String,Integer> exists = new ConcurrentHashMap<>();

    protected static final ThreadLocal<Message> localVar = new ThreadLocal<>();

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
        Message serviceMessage = localVar.get();
        if (serviceMessage != null && serviceMessage.getClazz().isAnnotationPresent(CacheBeanClass.class)
                && serviceMessage.getMethod().isAnnotationPresent(ServiceCache.class)) {
            //局部变量初始化与局部引用声明
            Lock readLock = null ;
            Lock writeLock = null;
            boolean access = false;
            Object result = null ;
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
            //该PO领域的初始化
            exists.putIfAbsent(poType, 0);
            try {
                //判断serviceMethod的是否为通过id获取数据
                //  若是则直接使用id进行获取
                //  若否则经过编码后进行获取
                if (methodStatus.equals(KacheAutoConfig.SERVICE_BY_ID)) {
                    Method methodGetId = serviceMessage.getArg().getClass().getMethod(METHOD_GET_ID, null);
                    key = methodGetId.invoke(serviceMessage.getArg()).toString() ;
                }else {
                    key = cacheEncoder.encode(serviceMessage.getArg(), methodStatus
                            , serviceMessage.getMethod(), beanClass.getName(), daoMethodName, daoArgs) ;
                }
                //使用循环和CAS对纵向的线程穿透进行线程执行限制，减少其重复获取写锁所带来的性能成本
                while (result == null) {
                    readLock = kacheLock.readLock(poType) ;
                    //获取缓存
                    result = baseCacheManager.get(key, beanClass);
                    kacheLock.unLock(readLock);
                    if (result == null) {
                        //用于错误发生处理，判断是否该线程成功CAS，成功时并发生错误则在错误处理中将exists设置回false，保证exists的事务性
                        access = exists.replace(poType,0,1) ;
                        if (access) {
                            //为了防止缓存击穿，所以并不使用异步增加缓存，而采用同步锁限制
                            writeLock = kacheLock.writeLock(poType);
                            result = point.proceed();
                            baseCacheManager.put(key, result, beanClass);
                            kacheLock.unLock(writeLock);
                            exists.put(poType, 0);
                            break;
                        }
                    }
                }
                //空值替换
                if (baseCacheManager.getNullValue().equals(result)) {
                    result = null ;
                }
                return result;
            }catch (Exception e) {
                if (access) {
                    exists.put(poType,0);
                }
                log.error(e.getMessage());
                e.printStackTrace();
            } finally {
                if (!kacheLock.isLock(readLock)) {
                    kacheLock.unLock(readLock);
                }
                if (!kacheLock.isLock(writeLock)) {
                    kacheLock.unLock(writeLock);
                }
            }
        } else {
            return point.proceed() ;
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
