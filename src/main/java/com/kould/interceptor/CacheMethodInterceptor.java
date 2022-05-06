package com.kould.interceptor;

import com.kould.annotation.*;
import com.kould.config.ListenerProperties;
import com.kould.config.Status;
import com.kould.core.CacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.RegexEntity;
import com.kould.handler.StrategyHandler;
import com.kould.manager.IBaseCacheManager;
import com.kould.entity.KacheMessage;
import com.kould.proxy.MethodPoint;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public final class CacheMethodInterceptor implements InvocationHandler {

    private final Object target;

    private final Class<?> entityClass;

    private final IBaseCacheManager baseCacheManager;

    private final StrategyHandler strategyHandler;

    private final ListenerProperties listenerProperties;

    private final CacheHandler cacheHandler;

    private final CacheEncoder cacheEncoder;

    private final RegexEntity regexEntity;

    public CacheMethodInterceptor(Object target, Class<?> entityClass, IBaseCacheManager baseCacheManager, StrategyHandler strategyHandler,
                                  ListenerProperties listenerProperties, CacheHandler cacheHandler, CacheEncoder cacheEncoder, RegexEntity regexEntity) {
        this.target = target;
        this.entityClass = entityClass;
        this.baseCacheManager = baseCacheManager;
        this.strategyHandler = strategyHandler;
        this.listenerProperties = listenerProperties;
        this.cacheHandler = cacheHandler;
        this.cacheEncoder = cacheEncoder;
        this.regexEntity = regexEntity;
    }


    /**
     *
     * @param proxy 表示要进行增强的对象
     * @param method 表示拦截的方法
     * @param args 数组表示参数列表
     * @return 执行结果
     * @throws Throwable 异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> mapperEntityClass = this.entityClass;
        //此处使用的Target为该类实例化时注入的target，以实现二次加工（比如MyBatis生成的实例再次由Kache实例化）
        MethodPoint methodPoint = new MethodPoint(this.target, args, method);
        String methodName = method.getName();
        String typeName = mapperEntityClass.getTypeName();

        if (method.isAnnotationPresent(DaoSelect.class) || Pattern.matches(regexEntity.getSelectRegex(),methodName)) {
            return cacheHandler.load(methodPoint, listenerProperties.isEnable(), baseCacheManager::daoRead
                    , baseCacheManager::daoWrite, cacheEncoder::getDaoKey, typeName
                    , getStatusForRegex(method, methodName));
        }
        if (method.isAnnotationPresent(DaoInsert.class) || Pattern.matches(regexEntity.getInsertRegex(),methodName)) {
            return strategyHandler.insert(methodPoint
                    ,getKacheMessage(method, mapperEntityClass, args, typeName));
        }
        if (method.isAnnotationPresent(DaoDelete.class) || Pattern.matches(regexEntity.getDeleteRegex(),methodName)) {
            return strategyHandler.delete(methodPoint
                    ,getKacheMessage(method, mapperEntityClass, args, typeName));
        }
        if (method.isAnnotationPresent(DaoUpdate.class) || Pattern.matches(regexEntity.getUpdateRegex(),methodName)) {
            return strategyHandler.update(methodPoint
                    ,getKacheMessage(method, mapperEntityClass, args, typeName));
        }
        return methodPoint.execute();
    }

    private KacheMessage getKacheMessage(Method method, Class<?> beanClass, Object[] args, String type) {
        String daoMethodName = method.getName() ;
        return KacheMessage.builder()
                .arg(args)
                .cacheClazz(beanClass)
                .methodName(daoMethodName)
                .types(type)
                .build();
    }

    /**
     * 当方法没有DaoSelect时，通过判断MethodName来进行方法状态判断
     * @param method 方法
     * @param methodName 方法名
     * @return Status方法状态
     */
    private Status getStatusForRegex(Method method, String methodName) {
        DaoSelect annotation = method.getAnnotation(DaoSelect.class);
        Status status;
        if (annotation == null) {
            if (Pattern.matches(regexEntity.getSelectStatusByIdRegex(), methodName)) {
                status = Status.BY_ID;
            } else {
                status = Status.BY_FIELD;
            }
        } else {
            status = annotation.status();
        }
        return status;
    }
}
