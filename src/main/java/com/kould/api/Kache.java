package com.kould.api;

import com.kould.entity.KeyEntity;
import com.kould.properties.*;
import com.kould.interceptor.CacheMethodInterceptor;
import com.kould.codec.KryoRedisCodec;
import com.kould.core.CacheHandler;
import com.kould.core.impl.BaseCacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.encoder.impl.BaseCacheEncoder;
import com.kould.strategy.Strategy;
import com.kould.strategy.impl.AmqpStrategy;
import com.kould.strategy.impl.DBFirstStrategy;
import com.kould.listener.impl.StatisticsListener;
import com.kould.manager.IBaseCacheManager;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.manager.impl.BaseCacheManagerImpl;
import com.kould.manager.impl.GuavaCacheManager;
import com.kould.manager.impl.RedisCacheManager;
import com.kould.service.RedisService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.RedisCodec;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Kache {

    private final ListenerProperties listenerProperties;

    public static final String INDEX_TAG = "#INDEX:";

    public static final String CACHE_PREFIX = "KACHE:";

    public static final String SPLIT_TAG = "&";

    private final KeyEntity keyEntity;

    private final CacheEncoder cacheEncoder;

    private final IBaseCacheManager iBaseCacheManager;

    private final RemoteCacheManager remoteCacheManager;

    private final RedisService redisService;

    private final Strategy strategy;

    private final CacheHandler cacheHandler;

    private static class BeanLoad {

        private Class<?> interfaceClass;

        private Class<?> accomplishClass;

        private Class<?>[] argsClass;

        public BeanLoad(Class<?> interfaceClass, Class<?> accomplishClass, Class<?>[] argsClass) {
            this.interfaceClass = interfaceClass;
            this.accomplishClass = accomplishClass;
            this.argsClass = argsClass;
        }

        public Class<?> getInterfaceClass() {
            return interfaceClass;
        }

        public Class<?> getAccomplishClass() {
            return accomplishClass;
        }

        public Class<?>[] getArgsClass() {
            return argsClass;
        }
    }

    public static class Builder implements com.kould.type.Builder<Kache> {

        private static final Map<Class<?>, Object> BEAN_BOOT = new HashMap<>();

        private static final Map<Class<?>, Kache.BeanLoad> BEAN_LOAD_MAP = new HashMap<>();

        private RedisClient redisClient;

        private CacheEncoder cacheEncoder;

        private CacheHandler cacheHandler;

        private RedisCodec<String, Object> redisCodec;

        private DaoProperties daoProperties;

        private DataFieldProperties dataFieldProperties;

        private InterprocessCacheProperties interprocessCacheProperties;

        private ListenerProperties listenerProperties;

        private KeyProperties keyProperties;

        private InterprocessCacheManager interprocessCacheManager;

        private RedisService redisService;

        private RemoteCacheManager remoteCacheManager;

        private IBaseCacheManager iBaseCacheManager;

        private Strategy strategy;

        public Builder() {
            StatisticsListener.getInstance();
            BEAN_BOOT.put(RedisClient.class, RedisClient.create(RedisURI.builder()
                    .withHost("localhost")
                    .withPort(6379)
                    .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                    .build()));
            BEAN_BOOT.put(CacheEncoder.class, BaseCacheEncoder.getInstance());
            BEAN_BOOT.put(CacheHandler.class, new BaseCacheHandler());
            BEAN_BOOT.put(RedisCodec.class, new KryoRedisCodec());
            BEAN_BOOT.put(DaoProperties.class, new DaoProperties());
            BEAN_BOOT.put(DataFieldProperties.class, new DataFieldProperties());
            BEAN_BOOT.put(InterprocessCacheProperties.class, new InterprocessCacheProperties());
            BEAN_BOOT.put(ListenerProperties.class, new ListenerProperties());
            BEAN_BOOT.put(KeyProperties.class, new KeyProperties());

            BEAN_LOAD_MAP.put(InterprocessCacheManager.class, new BeanLoad(InterprocessCacheManager.class, GuavaCacheManager.class, new Class[]{DaoProperties.class,InterprocessCacheProperties.class}));
            BEAN_LOAD_MAP.put(RedisService.class, new BeanLoad(RedisService.class, RedisService.class, new Class[]{DaoProperties.class, RedisClient.class, RedisCodec.class}));
            BEAN_LOAD_MAP.put(RemoteCacheManager.class, new BeanLoad(RemoteCacheManager.class, RedisCacheManager.class, new Class[]{DataFieldProperties.class, DaoProperties.class, RedisService.class, CacheEncoder.class}));
            BEAN_LOAD_MAP.put(IBaseCacheManager.class, new BeanLoad(IBaseCacheManager.class, BaseCacheManagerImpl.class, new Class[]{InterprocessCacheManager.class, RemoteCacheManager.class
                    , InterprocessCacheProperties.class, CacheEncoder.class, DataFieldProperties.class}));
            BEAN_LOAD_MAP.put(Strategy.class, new BeanLoad(Strategy.class, DBFirstStrategy.class, new Class[]{IBaseCacheManager.class}));
        }

        /**
         * 载入自定义或额外配置方法
         *
         * 用于自定义实体类时的额外参数构造
         * @param interfaceClass 目标参数类型，请勿传入同类型的配置，可以包装后传入
         * @param bean 配置实体
         * @return
         */
        public Kache.Builder load(Class<?> interfaceClass, Object bean) {
            BEAN_BOOT.put(interfaceClass, bean);
            return this;
        }

        /**
         * 替换默认实现类方法
         * 允许用户通过自定义实现对应需要替换的接口，将其载入到Kache中进行默认组件的替换
         *
         * 若使用自定义的配置或组件需要先使用load方法进行配置载入，否则Kache对实现类进行实例化时无法找到对应的配置
         * @param interfaceClass 需要对应替换的接口Class
         * @param accomplishClass 自定义的实现类Class
         * @param argsClass 该实现类对应的参数Class数组
         * @return
         */
        public Kache.Builder replace(Class<?> interfaceClass, Class<?> accomplishClass, Class<?>[] argsClass) {
            BEAN_LOAD_MAP.put(interfaceClass,new BeanLoad(interfaceClass, accomplishClass, argsClass));
            return this;
        }

        /**
         * Kache的建造方法，用于将载入的配置与BeanLoad进行组装
         * @return Kache对象
         */
        @Override
        public Kache build() {
            try {
                componentLoad(BEAN_LOAD_MAP.get(InterprocessCacheManager.class));
                componentLoad(BEAN_LOAD_MAP.get(RedisService.class));
                componentLoad(BEAN_LOAD_MAP.get(RemoteCacheManager.class));
                componentLoad(BEAN_LOAD_MAP.get(IBaseCacheManager.class));
                componentLoad(BEAN_LOAD_MAP.get(Strategy.class));

                for (Field declaredField : Builder.class.getDeclaredFields()) {
                    declaredField.setAccessible(true);
                    Object bean = BEAN_BOOT.get(declaredField.getType());
                    if (bean != null) {
                        declaredField.set(this, bean);
                    }
                }
                return new Kache(this) ;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * BeanLoad中的对应接口与实现类进行组件装填载入
         * @param beanLoad 实现类的类信息封装
         * @throws NoSuchMethodException
         * @throws InstantiationException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        private void componentLoad(BeanLoad beanLoad) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            Class<?> interfaceClass = beanLoad.getInterfaceClass();
            Class<?> accomplishClass = beanLoad.getAccomplishClass();
            Class<?>[] argsClass = beanLoad.getArgsClass();
            Constructor<?> constructor = accomplishClass.getConstructor(argsClass);
            Object[] realArgs = new Object[argsClass.length];
            for (int i = 0; i < argsClass.length; i++) {
                realArgs[i] = BEAN_BOOT.get(argsClass[i]);
            }
            BEAN_BOOT.put(interfaceClass, constructor.newInstance(realArgs));
        }
    }

    public static Builder builder() {
        return new Builder() ;
    }

    public Kache(Builder builder) {
        this.keyEntity = new KeyEntity(builder.keyProperties);
        this.cacheEncoder = builder.cacheEncoder;
        this.cacheHandler = builder.cacheHandler;
        this.iBaseCacheManager = builder.iBaseCacheManager;
        this.listenerProperties = builder.listenerProperties;
        this.redisService = builder.redisService;
        this.remoteCacheManager = builder.remoteCacheManager;
        this.strategy = builder.strategy;
    }

    /**
     * 通过该方法对目标Mapper进行缓存代理增强
     * @param target 目标Mapper
     * @param entityClass mapper对应的操作实体类
     * @param <T> 目标Mapper类型
     * @return 拥有缓存代理增强后的Mapper
     */
    public <T> T getProxy(T target,Class<?> entityClass) {
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces()
                , new CacheMethodInterceptor(target, entityClass, this.iBaseCacheManager
                        , this.strategy, this.listenerProperties, this.cacheHandler, this.cacheEncoder, this.keyEntity));
    }

    /**
     * Kache初始化方法
     * @throws Exception
     */
    public void init() throws Exception {
        if (strategy instanceof AmqpStrategy) {
            ((AmqpStrategy) strategy).init();
        }
        remoteCacheManager.init();
    }

    /**
     * Kache资源释放方法
     * @throws Exception
     */
    public void destroy() throws Exception {
        if (strategy instanceof AmqpStrategy) {
            ((AmqpStrategy) strategy).destroy();
        }
        redisService.shutdown();
    }
}
