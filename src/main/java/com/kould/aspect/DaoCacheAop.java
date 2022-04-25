package com.kould.aspect;

import com.kould.config.Kache;
import com.kould.config.ListenerProperties;
import com.kould.core.CacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.handler.StrategyHandler;
import com.kould.manager.IBaseCacheManager;
import com.kould.message.KacheMessage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
public final class DaoCacheAop {

    private static final Logger log = LoggerFactory.getLogger(DaoCacheAop.class) ;

    public static final Map<String,Class<?>> CLASS_MAP = new HashMap<>();

    private final IBaseCacheManager baseCacheManager;

    private final StrategyHandler strategyHandler;

    private final ListenerProperties listenerProperties;

    private final CacheHandler cacheHandler;

    private final CacheEncoder cacheEncoder;

    public DaoCacheAop(IBaseCacheManager baseCacheManager, StrategyHandler strategyHandler,
                       ListenerProperties listenerProperties, CacheHandler cacheHandler, CacheEncoder cacheEncoder) {
        this.baseCacheManager = baseCacheManager;
        this.strategyHandler = strategyHandler;
        this.listenerProperties = listenerProperties;
        this.cacheHandler = cacheHandler;
        this.cacheEncoder = cacheEncoder;
    }

    @Pointcut(Kache.POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_SELECT)
    public void pointCutMyBatisPlusFind() {
    }

    @Pointcut(Kache.POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_INSERT)
    public void pointCutMyBatisPlusAdd() {
    }

    @Pointcut(Kache.POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_DELETE)
    public void pointCutMyBatisPlusRemove() {
    }

    @Pointcut(Kache.POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_UPDATE)
    public void pointCutMyBatisPlusEdit() {
    }

    /**
     * 数据查询最上层中的缓存操作抽象逻辑切片处理
     * 用于主体上对缓存操作进行调控
     * @param point 数据操纵切入点
     * @return 缓存具体数据
     * @throws Throwable 可能存在的问题，由业务代码决定
     */
    @Around("@annotation(com.kould.annotation.DaoSelect) || pointCutMyBatisPlusFind()")
    public Object findAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Class<?> beanClass = CLASS_MAP.get(point.getTarget().toString());
        if (beanClass != null) {
            return cacheHandler.load(point, listenerProperties.isEnable(), baseCacheManager::daoRead
                    , baseCacheManager::daoWrite, cacheEncoder::getDaoKey, beanClass.getTypeName());
        } else {
            return point.proceed() ;
        }
    }

    @Around("@annotation(com.kould.annotation.DaoDelete) || pointCutMyBatisPlusRemove()")
    public Object removeAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        log.info(Kache.CACHE_PREFIX + "检测到数据删除");
        Class<?> beanClass = CLASS_MAP.get(point.getTarget().toString());
        if (beanClass != null) {
            return strategyHandler.delete(point, getKacheMessage(point, beanClass)) ;
        } else {
            return point.proceed() ;
        }
    }

    @Around("@annotation(com.kould.annotation.DaoInsert) || pointCutMyBatisPlusAdd()")
    public Object insertAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        log.info(Kache.CACHE_PREFIX + "检测到数据增加");
        Class<?> beanClass = CLASS_MAP.get(point.getTarget().toString());
        if (beanClass != null) {
            return strategyHandler.insert(point, getKacheMessage(point, beanClass)) ;
        } else {
            return point.proceed() ;
        }
    }

    @Around("@annotation(com.kould.annotation.DaoUpdate) || pointCutMyBatisPlusEdit()")
    public Object editAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        log.info(Kache.CACHE_PREFIX + "检测到数据修改");
        Class<?> beanClass = CLASS_MAP.get(point.getTarget().toString());
        if (beanClass != null) {
            return strategyHandler.update(point, getKacheMessage(point, beanClass)) ;
        } else {
            return point.proceed() ;
        }
    }

    private KacheMessage getKacheMessage(ProceedingJoinPoint point, Class<?> beanClass) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method daoMethod = methodSignature.getMethod();
        String daoMethodName = daoMethod.getName() ;
        return KacheMessage.builder()
                .arg(point.getArgs())
                .cacheClazz(beanClass)
                .methodName(daoMethodName)
                .build();
    }
}
