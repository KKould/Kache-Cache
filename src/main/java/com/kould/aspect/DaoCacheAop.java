package com.kould.aspect;

import com.kould.annotation.DaoSelect;
import com.kould.config.KacheAutoConfig;
import com.kould.config.ListenerProperties;
import com.kould.encoder.CacheEncoder;
import com.kould.handler.StrategyHandler;
import com.kould.listener.ListenerHandler;
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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Aspect
@Order(15)
public final class DaoCacheAop {

    private static final Logger log = LoggerFactory.getLogger(DaoCacheAop.class) ;

    //直接使用ReentrantLock而不使用接口Lock是因为需要判断锁状态
    private static final Map<String,ReentrantLock> REENTRANT_LOCK_MAP = new ConcurrentHashMap<>();

    protected static final ThreadLocal<KacheMessage> MESSAGE_THREAD_LOCAL_VAR = new ThreadLocal<>();

    private static final String AND = "||" ;
    @Autowired
    private KacheLock kacheLock ;

    @Autowired
    private IBaseCacheManager baseCacheManager;

    @Autowired
    private CacheEncoder cacheEncoder;

    @Autowired
    private StrategyHandler strategyHandler;

    @Autowired
    private ListenerProperties listenerProperties ;

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_SELECT)
    public void pointCutMyBatisPlusFind() {
    }

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_INSERT)
    public void pointCutMyBatisPlusAdd() {
    }

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_DELETE)
    public void pointCutMyBatisPlusRemove() {
    }

    @Pointcut(KacheAutoConfig.POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_UPDATE)
    public void pointCutMyBatisPlusEdit() {
    }


    @Around("@annotation(com.kould.annotation.DaoSelect) || pointCutMyBatisPlusFind()")
    public Object findAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Object result = null ;
        KacheMessage serviceMessage = MESSAGE_THREAD_LOCAL_VAR.get();
        if (serviceMessage != null) {
            Lock readLock = null ;
            Lock writeLock = null;
            boolean listenerEnable = listenerProperties.isEnable();
            String key = null;
            Class<?> beanClass = serviceMessage.getCacheClazz();
            //以PO类型进行不同持久类领域的划分，以此减少不必要的干涉开销并统一DTO的持久化操作
            String poType = beanClass.getTypeName();
            String lockKey = poType + serviceMessage.getMethodName() ;
            //该PO领域的初始化
            //通过lambda表达式延迟加载锁并获取
            ReentrantLock methodLock = REENTRANT_LOCK_MAP.computeIfAbsent(lockKey,k -> new ReentrantLock()) ;
            try {
                //key拼接命名空间前缀
                key = KacheAutoConfig.CACHE_PREFIX + getKey(point, serviceMessage, beanClass);
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
                        //此处为真正未命中处，若置于上层则可能导致缓存穿透的线程一起被计数而导致不够准确
                        ListenerHandler.notHit(key,serviceMessage,listenerEnable);
                        writeLock = kacheLock.writeLock(poType);
                        result = point.proceed();
                        baseCacheManager.put(key, result, beanClass);
                        kacheLock.unLock(writeLock);
                    } else {
                        //将同步后获取缓存的线程的命中也计数
                        ListenerHandler.hit(key,serviceMessage,listenerEnable);
                    }
                } else {
                    ListenerHandler.hit(key,serviceMessage,listenerEnable);
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
    private String getKey(ProceedingJoinPoint point, KacheMessage serviceMessage, Class<?> beanClass) {
        //判断serviceMethod的是否为通过id获取数据
        //  若是则直接使用id进行获取
        //  若否则经过编码后进行获取
        //信息摘要收集
        //获取DAO方法签名
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method daoMethod = methodSignature.getMethod();
        String daoMethodName = daoMethod.getName() ;
        if (daoMethodName.equals(KacheAutoConfig.MYBATIS_PLUS_MAPPER_SELECT_BY_ID)) {
            return setKey2Id(point);
        }
        DaoSelect daoSelect = daoMethod.getAnnotation(DaoSelect.class);
        String methodStatus = null ;
        if (daoSelect != null) {
            methodStatus = daoSelect.status().getValue();
        }
        if (methodStatus != null && methodStatus.equals(KacheAutoConfig.SERVICE_BY_ID)) {
            //使Key为ID
            return setKey2Id(point);
        }else {
            String daoArgs = cacheEncoder.argsEncode(point.getArgs());
            //使Key为各个参数编码后的一个特殊值
            return cacheEncoder.encode(methodStatus
                    ,serviceMessage.getMethodName() , beanClass.getName(), daoMethodName, daoArgs) ;
        }
    }

    private String setKey2Id(ProceedingJoinPoint point) {
        //使Key为ID
        Object[] args = point.getArgs();
        return args[0].toString();
    }

    @Around("@annotation(com.kould.annotation.DaoDelete) || pointCutMyBatisPlusRemove()")
    public Object removeAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        log.info(KacheAutoConfig.CACHE_PREFIX + "检测到数据删除");
        return strategyHandler.delete(point,MESSAGE_THREAD_LOCAL_VAR.get()) ;
    }

    @Around("@annotation(com.kould.annotation.DaoInsert) || pointCutMyBatisPlusAdd()")
    public Object insertAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        log.info(KacheAutoConfig.CACHE_PREFIX + "检测到数据增加");
        return strategyHandler.insert(point,MESSAGE_THREAD_LOCAL_VAR.get()) ;
    }

    @Around("@annotation(com.kould.annotation.DaoUpdate) || pointCutMyBatisPlusEdit()")
    public Object editAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        log.info(KacheAutoConfig.CACHE_PREFIX + "检测到数据修改");
        return strategyHandler.update(point,MESSAGE_THREAD_LOCAL_VAR.get()) ;
    }

}
