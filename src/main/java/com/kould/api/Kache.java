package com.kould.api;

import com.kould.entity.KeyEntity;
import com.kould.entity.PageDetails;
import com.kould.exception.KacheBuildException;
import com.kould.listener.ListenerHandler;
import com.kould.lock.KacheLock;
import com.kould.lock.impl.LocalLock;
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
import com.kould.manager.LocalCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.manager.impl.BaseCacheManagerImpl;
import com.kould.manager.impl.GuavaCacheManager;
import com.kould.manager.impl.RedisCacheManager;
import com.kould.service.RedisService;
import com.kould.utils.FieldUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.RedisCodec;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Kache {

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
            beanBoot.put(LocalCacheProperties.class, new LocalCacheProperties());
            beanBoot.put(ListenerProperties.class, new ListenerProperties());
            beanBoot.put(KeyProperties.class, new KeyProperties());
            beanBoot.put(LocalCacheManager.class, new GuavaCacheManager());
            beanBoot.put(IBaseCacheManager.class, new BaseCacheManagerImpl());
            beanBoot.put(Strategy.class, new DBFirstStrategy());
            beanBoot.put(CacheHandler.class, new BaseCacheHandler());
            beanBoot.put(RemoteCacheManager.class, new RedisCacheManager());
            beanBoot.put(KacheLock.class, new LocalLock());
        }

        public Kache.Builder page(Class<?> clazz, String fieldName, Class<?> fieldClass) throws NoSuchFieldException, IllegalAccessException {
            beanBoot.put(PageDetails.class, new PageDetails<>(clazz, fieldName, fieldClass));
            return this;
        }

        /**
         * ????????????????????????????????????
         *
         * ????????????????????????????????????????????????
         * @param interfaceClass ???????????????????????????????????????????????????????????????????????????
         * @param bean ????????????
         * @return Kache.Builder
         */
        public Kache.Builder load(Class<?> interfaceClass, Object bean) {
            beanBoot.put(interfaceClass, bean);
            return this;
        }

        /**
         * Kache?????????????????????????????????????????????BeanLoad????????????
         * @return Kache??????
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
         * ???beanBoot??????BeanLoad????????????????????????
         * @throws IllegalAccessException ??????????????????
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
         * ????????????RedisService
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
     * ????????????????????????Mapper????????????????????????
     * @param target ??????Mapper
     * @param entityClass mapper????????????????????????
     * @param <T> ??????Mapper??????
     * @return ??????????????????????????????Mapper
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
     * Kache???????????????
     * @throws Exception ???????????????
     */
    public void init() throws Exception {
        if (strategy instanceof AmqpStrategy) {
            ((AmqpStrategy) strategy).init();
        }
        remoteCacheManager.init();
    }

    /**
     * Kache??????????????????
     * @throws Exception ????????????
     */
    public void destroy() throws Exception {
        if (strategy instanceof AmqpStrategy) {
            ((AmqpStrategy) strategy).destroy();
        }
        redisService.shutdown();
    }

    /**
     * Kache????????????
     * @return Map ??????????????????????????????
     */
    public Map<String,Object> message() {
        Map<String, Object> message = new HashMap<>();
        message.put("listener", ListenerHandler.details());
        return message;
    }
}
