package com.kould.aspect;

import com.kould.config.KacheAutoConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Order(15)
public final class DaoCacheAop {

    private static final Logger log = LoggerFactory.getLogger(DaoCacheAop.class) ;

    public static final Map<String,Class<?>> CLASS_MAP = new HashMap<>();

    @Autowired
    private IBaseCacheManager baseCacheManager;

    @Autowired
    private StrategyHandler strategyHandler;

    @Autowired
    private ListenerProperties listenerProperties ;

    @Autowired
    private CacheHandler cacheHandler;

    @Autowired
    private CacheEncoder cacheEncoder;

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

    /**
     * 数据查询最上层中的缓存操作抽象逻辑切片处理
     * 用于主体上对缓存操作进行调控
     * @param point
     * @return 缓存具体数据
     * @throws Throwable
     */
    @Around("@annotation(com.kould.annotation.DaoSelect) || pointCutMyBatisPlusFind()")
    public Object findAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        Class<?> beanClass = CLASS_MAP.get(point.getTarget().toString());
        if (beanClass != null) {
            return cacheHandler.load(point, beanClass, listenerProperties.isEnable(), baseCacheManager::daoRead
                    , baseCacheManager::daoWrite, cacheEncoder::getDaoKey, beanClass.getName());
        } else {
            return point.proceed() ;
        }
    }

    @Around("@annotation(com.kould.annotation.DaoDelete) || pointCutMyBatisPlusRemove()")
    public Object removeAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        log.info(KacheAutoConfig.CACHE_PREFIX + "检测到数据删除");
        Class<?> beanClass = CLASS_MAP.get(point.getTarget());
        if (beanClass != null) {
            return strategyHandler.delete(point, getKacheMessage(point, beanClass)) ;
        } else {
            return point.proceed() ;
        }
    }

    @Around("@annotation(com.kould.annotation.DaoInsert) || pointCutMyBatisPlusAdd()")
    public Object insertAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        log.info(KacheAutoConfig.CACHE_PREFIX + "检测到数据增加");
        Class<?> beanClass = CLASS_MAP.get(point.getTarget());
        if (beanClass != null) {
            return strategyHandler.insert(point, getKacheMessage(point, beanClass)) ;
        } else {
            return point.proceed() ;
        }
    }

    @Around("@annotation(com.kould.annotation.DaoUpdate) || pointCutMyBatisPlusEdit()")
    public Object editAroundInvoke(ProceedingJoinPoint point) throws Throwable {
        log.info(KacheAutoConfig.CACHE_PREFIX + "检测到数据修改");
        Class<?> beanClass = CLASS_MAP.get(point.getTarget());
        if (beanClass != null) {
            return strategyHandler.update(point, getKacheMessage(point, beanClass)) ;
        } else {
            return point.proceed() ;
        }
    }

    private KacheMessage getKacheMessage(ProceedingJoinPoint point, Class<?> beanClass) throws Exception {
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
