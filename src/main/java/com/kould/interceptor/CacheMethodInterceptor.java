package com.kould.interceptor;

import com.kould.annotation.*;
import com.kould.api.Kache;
import com.kould.api.KacheEntity;
import com.kould.entity.*;
import com.kould.core.CacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.strategy.Strategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 缓存方法环绕代理调用处理器
 */
public final class CacheMethodInterceptor implements InvocationHandler {

    private final Object target;

    private final Class<? extends KacheEntity> entityClass;

    private final Strategy strategy;

    private final CacheHandler cacheHandler;

    private final CacheEncoder cacheEncoder;

    private final KeyEntity keyEntity;

    public CacheMethodInterceptor(Builder builder) {
        this.target = builder.target;
        this.entityClass = builder.entityClass;
        this.strategy = builder.strategy;
        this.cacheHandler = builder.cacheHandler;
        this.cacheEncoder = builder.cacheEncoder;
        this.keyEntity = builder.keyEntity;
    }


    /**
     * 对CRUD方法进行区分并对应处理
     *
     * 读取时：
     * 若使用了级联功能则对类型进行级联拼接处理
     * 并通过CacheHandler进行缓存的读出
     *
     * 增删改时：
     * 将方法摘要进行封装并传入Strategy之中用于进一步处理
     * @param proxy 表示要进行增强的对象
     * @param method 表示拦截的方法
     * @param args 数组表示参数列表
     * @return 执行结果
     * @throws Throwable 异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<? extends KacheEntity> mapperEntityClass = this.entityClass;
        //此处使用的Target为该类实例化时注入的target，以实现二次加工（比如MyBatis生成的实例再次由Kache实例化）
        MethodPoint methodPoint = new MethodPoint(this.target, args, method);
        String methodName = method.getName();
        String typeName = mapperEntityClass.getTypeName();
        if (method.isAnnotationPresent(DaoSelect.class) || keyEntity.selectKeyMatch(methodName)) {
            DaoSelect daoSelect = method.getAnnotation(DaoSelect.class);
            typeName = typeSuperposition(typeName, daoSelect);
            return cacheHandler.load(methodPoint, typeName, getStatus(daoSelect, methodName));
        }
        if (method.isAnnotationPresent(DaoInsert.class) || keyEntity.insertKeyMatch(methodName)) {
            return strategy.insert(methodPoint
                    ,getKacheMessage(method, mapperEntityClass, args, typeName));
        }
        if (method.isAnnotationPresent(DaoDelete.class) || keyEntity.deleteKeyMatch(methodName)) {
            return strategy.delete(methodPoint
                    ,getKacheMessage(method, mapperEntityClass, args, typeName));
        }
        if (method.isAnnotationPresent(DaoUpdate.class) || keyEntity.updateKeyMatch(methodName)) {
            return strategy.update(methodPoint
                    ,getKacheMessage(method, mapperEntityClass, args, typeName));
        }
        return methodPoint.execute();
    }

    /**
     * 类型叠加仅仅针对于索引缓存
     * 本身仅能保证能使索引缓存被多个Bean域干涉，但是可能存在有潜在的其他查询导致外键实体变化导致信息不准确。
     * 不鼓励使用持久层涉及多表查询，在非用不可的情况下尽可能不使用缓存注解，除非进行手动的Bean处理
     *
     * @param mainType 主类名
     * @param daoSelect 持久层搜索方法注解
     * @return 多类型联合字符串
     */
    private String typeSuperposition(String mainType, DaoSelect daoSelect) {
        if (daoSelect != null && daoSelect.involve().length > 0 && Status.BY_FIELD.equals(daoSelect.status())) {
            // 多关联Bean的类型表示增加
            StringBuilder typeNameBuilder = new StringBuilder(mainType);
            for (Class<?> clazz : daoSelect.involve()) {
                typeNameBuilder.append(Kache.SPLIT_TAG).append(clazz.getTypeName());
            }
            mainType = typeNameBuilder.toString();
        }
        return mainType;
    }

    /**
     * KacheMessage组装并生成唯一ID作为幂等凭据
     * @param method 方法
     * @param beanClass Bean类型
     * @param args 参数
     * @param type Bean领域(字符串)
     * @return KacheMessage
     */
    private KacheMessage getKacheMessage(Method method, Class<? extends KacheEntity> beanClass, Object[] args, String type) {
        String messageId = cacheEncoder.getId2Key(UUID.randomUUID().toString(), type);
        String daoMethodName = method.getName() ;
        return KacheMessage.builder()
                .id(messageId)
                .args(args)
                .cacheClazz(beanClass)
                .methodName(daoMethodName)
                .type(type)
                .build();
    }

    /**
     * 当方法没有DaoSelect时，通过判断MethodName来进行方法状态判断
     * @param daoSelect 注解
     * @param methodName 方法名
     * @return Status方法状态
     */
    private Status getStatus(DaoSelect daoSelect, String methodName) {
        Status status;
        if (daoSelect == null) {
            if (keyEntity.selectByIdKeyEquals(methodName)) {
                status = Status.BY_ID;
            } else {
                status = Status.BY_FIELD;
            }
        } else {
            status = daoSelect.status();
        }
        return status;
    }

    public static Builder builder() {
        return new Builder() ;
    }

    public static class Builder {

        private Object target;

        private Class<? extends KacheEntity> entityClass;

        private Strategy strategy;

        private CacheHandler cacheHandler;

        private CacheEncoder cacheEncoder;

        private KeyEntity keyEntity;

        public CacheMethodInterceptor.Builder mapper(Object mapper) {
            this.target = mapper;
            return this;
        }

        public CacheMethodInterceptor.Builder entityClass(Class<? extends KacheEntity> entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public CacheMethodInterceptor.Builder strategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public CacheMethodInterceptor.Builder cacheHandler(CacheHandler cacheHandler) {
            this.cacheHandler = cacheHandler;
            return this;
        }

        public CacheMethodInterceptor.Builder cacheEncoder(CacheEncoder cacheEncoder) {
            this.cacheEncoder = cacheEncoder;
            return this;
        }

        public CacheMethodInterceptor.Builder keyEntity(KeyEntity keyEntity) {
            this.keyEntity = keyEntity;
            return this;
        }

        public CacheMethodInterceptor build() {
            return new CacheMethodInterceptor(this);
        }
    }
}
