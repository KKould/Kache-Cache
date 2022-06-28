package com.kould.api;

import com.kould.entity.KeyEntity;
import com.kould.entity.PageDetails;
import com.kould.exception.KacheBuildException;
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
import com.kould.utils.FieldUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.RedisCodec;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Kache {

    public static final Objenesis OBJENESIS = new ObjenesisStd();

    public static final String INDEX_TAG = "#INDEX:";

    public static final String CACHE_PREFIX = "KACHE:";

    public static final String SPLIT_TAG = "&";

    public static final String PAGE_UNLOAD_MESSAGE = "The Page's Class unload!";

    private final KeyEntity keyEntity;

    private final CacheEncoder cacheEncoder;

    private final RemoteCacheManager remoteCacheManager;

    private final RedisService redisService;

    private final Strategy strategy;

    private final CacheHandler cacheHandler;

    public static class Builder {

        private final Map<Class<?>, Object> beanBoot = new HashMap<>();

        private CacheEncoder cacheEncoder;

        private CacheHandler cacheHandler;

        private KeyProperties keyProperties;

        private RedisService redisService;

        private RemoteCacheManager remoteCacheManager;

        private Strategy strategy;

        public Builder() {
            StatisticsListener.getInstance();
            beanBoot.put(RedisClient.class, RedisClient.create(RedisURI.builder()
                    .withHost("localhost")
                    .withPort(6379)
                    .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                    .build()));
            beanBoot.put(CacheEncoder.class, BaseCacheEncoder.getInstance());
            beanBoot.put(RedisCodec.class, new KryoRedisCodec());
            beanBoot.put(DaoProperties.class, new DaoProperties());
            beanBoot.put(InterprocessCacheProperties.class, new InterprocessCacheProperties());
            beanBoot.put(ListenerProperties.class, new ListenerProperties());
            beanBoot.put(KeyProperties.class, new KeyProperties());
            beanBoot.put(InterprocessCacheManager.class, new GuavaCacheManager());
            beanBoot.put(IBaseCacheManager.class, new BaseCacheManagerImpl());
            beanBoot.put(Strategy.class, new DBFirstStrategy());
            beanBoot.put(CacheHandler.class, new BaseCacheHandler());
            beanBoot.put(RemoteCacheManager.class, new RedisCacheManager());

        }

        /**
         * 载入自定义或额外配置方法
         *
         * 用于自定义实体类时的额外参数构造
         * @param interfaceClass 目标参数类型，请勿传入同类型的配置，可以包装后传入
         * @param bean 配置实体
         * @return Kache.Builder
         */
        public Kache.Builder load(Class<?> interfaceClass, Object bean) {
            beanBoot.put(interfaceClass, bean);
            return this;
        }

        /**
         * Kache的建造方法，用于将载入的配置与BeanLoad进行组装
         * @return Kache对象
         */
        public Kache build() throws IllegalAccessException {
            PageDetails<?> pageDetails = (PageDetails<?>) beanBoot.get(PageDetails.class);
            if (pageDetails == null) {
                throw new KacheBuildException(PAGE_UNLOAD_MESSAGE);
            }
            if (beanBoot.get(RedisService.class) == null) {
                injectionDefaultRedisService();
            }
            beanInjectionOfBeanLoad();

            this.redisService = (RedisService) beanBoot.get(RedisService.class);
            this.strategy = (Strategy) beanBoot.get(Strategy.class);
            this.remoteCacheManager = (RemoteCacheManager) beanBoot.get(RemoteCacheManager.class);
            this.keyProperties = (KeyProperties) beanBoot.get(KeyProperties.class);
            this.cacheEncoder = (CacheEncoder) beanBoot.get(CacheEncoder.class);
            this.cacheHandler = (CacheHandler) beanBoot.get(CacheHandler.class);
            return new Kache(this);
        }

        /**
         * 对beanBoot内的BeanLoad对象进行组件注入
         * @throws IllegalAccessException 无法访问异常
         */
        private void beanInjectionOfBeanLoad() throws IllegalAccessException {
            for (Map.Entry<Class<?>, Object> entry : beanBoot.entrySet()) {
                Object bean = entry.getValue();
                if (bean instanceof BeanLoad) {
                    Class<?>[] injectionFields = ((BeanLoad) bean).loadArgs();
                    List<Field> allField = FieldUtils.getAllField(bean.getClass());
                    Set<Class<?>> classSet = new HashSet<>(Arrays.asList(injectionFields));
                    for (Field field : allField) {
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        if (classSet.contains(fieldType) && field.get(bean) == null) {
                            field.set(bean, beanBoot.get(fieldType));
                        }
                    }
                }
            }
        }

        /**
         * 注入默认RedisService
         */
        private void injectionDefaultRedisService() {
            DaoProperties daoProperties = (DaoProperties) beanBoot.get(DaoProperties.class);
            RedisClient redisClient = (RedisClient) beanBoot.get(RedisClient.class);
            RedisCodec<String, Object> redisCodec = (RedisCodec<String, Object>) beanBoot.get(RedisCodec.class);
            beanBoot.put(RedisService.class, new RedisService(daoProperties, redisClient, redisCodec));
        }
    }

    public static Builder builder() {
        return new Builder() ;
    }

    public Kache(Builder builder) {
        this.keyEntity = new KeyEntity(builder.keyProperties);
        this.cacheEncoder = builder.cacheEncoder;
        this.cacheHandler = builder.cacheHandler;
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
    public <T> T getProxy(T target, Class<? extends KacheEntity> entityClass){
        CacheMethodInterceptor interceptor = CacheMethodInterceptor.builder()
                .mapper(target)
                .entityClass(entityClass)
                .strategy(this.strategy)
                .cacheHandler(this.cacheHandler)
                .cacheEncoder(this.cacheEncoder)
                .keyEntity(this.keyEntity)
                .build();
        ClassLoader classLoader = target.getClass().getClassLoader();
        Class<?>[] interfaces = target.getClass().getInterfaces();
        return (T) Proxy.newProxyInstance(classLoader, interfaces, interceptor);
    }

    /**
     * Kache初始化方法
     * @throws Throwable 初始化异常
     */
    public void init() throws Throwable {
        if (strategy instanceof AmqpStrategy) {
            ((AmqpStrategy) strategy).init();
        }
        remoteCacheManager.init();
    }

    /**
     * Kache资源释放方法
     * @throws Throwable 释放异常
     */
    public void destroy() throws Throwable {
        if (strategy instanceof AmqpStrategy) {
            ((AmqpStrategy) strategy).destroy();
        }
        redisService.shutdown();
    }
}
