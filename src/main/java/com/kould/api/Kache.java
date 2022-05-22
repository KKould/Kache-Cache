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
import com.kould.strategy.impl.DBFirst;
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
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Kache {

    private final ListenerProperties listenerProperties;

    public static final String INDEX_TAG = "#INDEX:";

    public static final String CACHE_PREFIX = "KACHE:";

    private final KeyEntity keyEntity;

    private final CacheEncoder cacheEncoder;

    private final IBaseCacheManager iBaseCacheManager;

    private final RemoteCacheManager remoteCacheManager;

    private final RedisService redisService;

    private final Strategy strategy;

    private final CacheHandler cacheHandler;

    public static class Builder implements com.kould.type.Builder<Kache> {

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
            redisClient = RedisClient.create(RedisURI.builder()
                    .withHost("localhost")
                    .withPort(6379)
                    .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                    .build());
            cacheEncoder = BaseCacheEncoder.getInstance();
            cacheHandler = new BaseCacheHandler();
            redisCodec = new KryoRedisCodec();
            daoProperties = new DaoProperties();
            dataFieldProperties = new DataFieldProperties();
            interprocessCacheProperties = new InterprocessCacheProperties();
            listenerProperties = new ListenerProperties();
            keyProperties = new KeyProperties();
            interprocessCacheManager = new GuavaCacheManager(daoProperties, interprocessCacheProperties);
            redisService = new RedisService(daoProperties, redisClient, redisCodec);
            remoteCacheManager = new RedisCacheManager(dataFieldProperties, daoProperties
                    , redisService, cacheEncoder);
            iBaseCacheManager = new BaseCacheManagerImpl(interprocessCacheManager
                    , remoteCacheManager, interprocessCacheProperties,cacheEncoder,dataFieldProperties);
            strategy = new DBFirst(iBaseCacheManager);
            StatisticsListener.newInstance();
        }

        public Kache.Builder redisClient(RedisClient redisClient) {
            this.redisClient = redisClient;
            return this;
        }

        public Kache.Builder cacheEncoder(CacheEncoder cacheEncoder) {
            this.cacheEncoder = cacheEncoder;
            return this;
        }

        public Kache.Builder iBaseCacheManager(IBaseCacheManager iBaseCacheManager) {
            this.iBaseCacheManager = iBaseCacheManager;
            return this;
        }

        public Kache.Builder remoteCacheManager(RemoteCacheManager remoteCacheManager) {
            this.remoteCacheManager = remoteCacheManager;
            return this;
        }

        public Kache.Builder redisService(RedisService redisService) {
            this.redisService = redisService;
            return this;
        }

        public Kache.Builder interprocessCacheManager(InterprocessCacheManager interprocessCacheManager) {
            this.interprocessCacheManager = interprocessCacheManager;
            return this;
        }

        public Kache.Builder strategyHandler(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Kache.Builder cacheHandler(CacheHandler cacheHandler) {
            this.cacheHandler = cacheHandler;
            return this;
        }

        public Kache.Builder redisCodec(RedisCodec<String, Object> redisCodec) {
            this.redisCodec = redisCodec;
            return this;
        }

        public Kache.Builder daoProperties(DaoProperties daoProperties) {
            this.daoProperties = daoProperties;
            return this;
        }

        public Kache.Builder dataFieldProperties(DataFieldProperties dataFieldProperties) {
            this.dataFieldProperties = dataFieldProperties;
            return this;
        }

        public Kache.Builder interprocessCacheProperties(InterprocessCacheProperties interprocessCacheProperties) {
            this.interprocessCacheProperties = interprocessCacheProperties;
            return this;
        }

        public Kache.Builder listenerProperties(ListenerProperties listenerProperties) {
            this.listenerProperties = listenerProperties;
            return this;
        }

        public Kache.Builder keyProperties(KeyProperties keyProperties) {
            this.keyProperties = keyProperties;
            return this;
        }

        @Override
        public Kache build() {
            return new Kache(this) ;
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

    public <T> T getProxy(T target,Class<?> entityClass) {
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces()
                , new CacheMethodInterceptor(target, entityClass, this.iBaseCacheManager
                        , this.strategy, this.listenerProperties, this.cacheHandler, this.cacheEncoder
                        , keyEntity));
    }

    public void init() throws Exception {
        remoteCacheManager.init();
    }

    public void destroy() {
        redisService.shutdown();
    }
}
