package com.kould.api;

import com.kould.entity.KeyEntity;
import com.kould.properties.DaoProperties;
import com.kould.properties.DataFieldProperties;
import com.kould.properties.InterprocessCacheProperties;
import com.kould.properties.ListenerProperties;
import com.kould.interceptor.CacheMethodInterceptor;
import com.kould.codec.KryoRedisCodec;
import com.kould.core.CacheHandler;
import com.kould.core.impl.BaseCacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.encoder.impl.BaseCacheEncoder;
import com.kould.handler.StrategyHandler;
import com.kould.handler.impl.DBFirstHandler;
import com.kould.listener.CacheListener;
import com.kould.listener.impl.StatisticsListener;
import com.kould.lock.KacheLock;
import com.kould.lock.impl.LocalLock;
import com.kould.logic.CacheLogic;
import com.kould.logic.impl.BaseCacheLogic;
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

    private final DaoProperties daoProperties;

    private final DataFieldProperties dataFieldProperties;

    private final InterprocessCacheProperties interprocessCacheProperties;

    private final ListenerProperties listenerProperties;

    public static final String INDEX_TAG = "$INDEX:";

    public static final String CACHE_PREFIX = "KACHE:";

    public static final String DEFAULT_SELECT_KEY = "select";

    public static final String DEFAULT_INSERT_KEY = "insert";

    public static final String DEFAULT_DELETE_KEY = "delete";

    public static final String DEFAULT_UPDATE_KEY = "update";

    public static final String DEFAULT_SELECT_BY_ID_KEY = "selectById";

    private final String selectKey;

    private final String insertKey;

    private final String deleteKey;

    private final String updateKey;

    private final String selectStatusByIdKey;

    private final CacheEncoder cacheEncoder;

    private final IBaseCacheManager iBaseCacheManager;

    private final RemoteCacheManager remoteCacheManager;

    private final RedisService redisService;

    private final InterprocessCacheManager interprocessCacheManager;

    private final KacheLock kacheLock;

    private final StrategyHandler strategyHandler;

    private final CacheLogic cacheLogic;

    private final CacheHandler cacheHandler;

    private final RedisCodec<String, Object> redisCodec;

    private final CacheListener cacheListener = StatisticsListener.newInstance();

    public static class Builder implements com.kould.type.Builder<Kache> {

        private String selectKey = Kache.DEFAULT_SELECT_KEY;

        private String insertKey = Kache.DEFAULT_INSERT_KEY;

        private String deleteKey = Kache.DEFAULT_DELETE_KEY;

        private String updateKey = Kache.DEFAULT_UPDATE_KEY;

        private String selectStatusByIdKey = Kache.DEFAULT_SELECT_BY_ID_KEY;

        private RedisClient redisClient = RedisClient.create(RedisURI.builder()
                .withHost("localhost")
                .withPort(6379)
                .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                .build());

        private CacheEncoder cacheEncoder = BaseCacheEncoder.getInstance();

        private KacheLock kacheLock = new LocalLock();

        private CacheHandler cacheHandler = new BaseCacheHandler();

        private RedisCodec<String, Object> redisCodec = new KryoRedisCodec();

        private DaoProperties daoProperties = new DaoProperties();

        private DataFieldProperties dataFieldProperties = new DataFieldProperties();

        private InterprocessCacheProperties interprocessCacheProperties = new InterprocessCacheProperties();

        private ListenerProperties listenerProperties = new ListenerProperties();

        private InterprocessCacheManager interprocessCacheManager = new GuavaCacheManager(daoProperties
                , interprocessCacheProperties);

        private RedisService redisService = new RedisService(daoProperties, redisClient, redisCodec);

        private RemoteCacheManager remoteCacheManager = new RedisCacheManager(dataFieldProperties, daoProperties
                , redisService, kacheLock, cacheEncoder);

        private CacheLogic cacheLogic = new BaseCacheLogic(kacheLock, cacheEncoder, remoteCacheManager
                , interprocessCacheManager, dataFieldProperties);

        private StrategyHandler strategyHandler = new DBFirstHandler(daoProperties, cacheLogic);

        private IBaseCacheManager iBaseCacheManager = new BaseCacheManagerImpl(interprocessCacheManager
                , remoteCacheManager, interprocessCacheProperties);

        public Builder() { }

        public Kache.Builder selectKey(String selectKey) {
            this.selectKey = selectKey;
            return this;
        }

        public Kache.Builder insertKey(String insertKey) {
            this.insertKey = insertKey;
            return this;
        }

        public Kache.Builder deleteKey(String deleteKey) {
            this.deleteKey = deleteKey;
            return this;
        }

        public Kache.Builder updateKey(String updateKey) {
            this.updateKey = updateKey;
            return this;
        }

        public Kache.Builder selectStatusByIdKey(String selectStatusByIdKey) {
            this.selectStatusByIdKey = selectStatusByIdKey;
            return this;
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

        public Kache.Builder KacheLock(KacheLock kacheLock) {
            this.kacheLock = kacheLock;
            return this;
        }

        public Kache.Builder strategyHandler(StrategyHandler strategyHandler) {
            this.strategyHandler = strategyHandler;
            return this;
        }

        public Kache.Builder cacheLogic(CacheLogic cacheLogic) {
            this.cacheLogic = cacheLogic;
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

        @Override
        public Kache build() {
            return new Kache(this) ;
        }
    }

    public static Builder builder() {
        return new Builder() ;
    }

    public Kache(Builder builder) {
        this.selectKey = builder.selectKey;
        this.insertKey = builder.insertKey;
        this.deleteKey = builder.deleteKey;
        this.updateKey = builder.updateKey;
        this.selectStatusByIdKey = builder.selectStatusByIdKey;
        this.cacheEncoder = builder.cacheEncoder;
        this.cacheHandler = builder.cacheHandler;
        this.cacheLogic = builder.cacheLogic;
        this.daoProperties = builder.daoProperties;
        this.dataFieldProperties = builder.dataFieldProperties;
        this.iBaseCacheManager = builder.iBaseCacheManager;
        this.interprocessCacheProperties = builder.interprocessCacheProperties;
        this.interprocessCacheManager = builder.interprocessCacheManager;
        this.listenerProperties = builder.listenerProperties;
        this.kacheLock = builder.kacheLock;
        this.redisCodec = builder.redisCodec;
        this.redisService = builder.redisService;
        this.remoteCacheManager = builder.remoteCacheManager;
        this.strategyHandler = builder.strategyHandler;
    }

    public <T> T getProxy(T target,Class<?> entityClass) {
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), new CacheMethodInterceptor(target, entityClass, this.iBaseCacheManager
                , this.strategyHandler, this.listenerProperties, this.cacheHandler, this.cacheEncoder
                , new KeyEntity(selectKey ,insertKey ,deleteKey ,updateKey, selectStatusByIdKey)));
    }

    public void init() throws Exception {
        remoteCacheManager.init();
    }

    public void destroy() {
        redisService.shutdown();
    }

    public DaoProperties getDaoProperties() {
        return daoProperties;
    }

    public DataFieldProperties getDataFieldProperties() {
        return dataFieldProperties;
    }

    public InterprocessCacheProperties getInterprocessCacheProperties() {
        return interprocessCacheProperties;
    }

    public ListenerProperties getListenerProperties() {
        return listenerProperties;
    }

    public CacheEncoder getCacheEncoder() {
        return cacheEncoder;
    }

    public IBaseCacheManager getiBaseCacheManager() {
        return iBaseCacheManager;
    }

    public RemoteCacheManager getRemoteCacheManager() {
        return remoteCacheManager;
    }

    public RedisService getRedisService() {
        return redisService;
    }

    public InterprocessCacheManager getInterprocessCacheManager() {
        return interprocessCacheManager;
    }

    public KacheLock getKacheLock() {
        return kacheLock;
    }

    public StrategyHandler getStrategyHandler() {
        return strategyHandler;
    }

    public CacheLogic getCacheLogic() {
        return cacheLogic;
    }

    public CacheHandler getCacheHandler() {
        return cacheHandler;
    }

    public RedisCodec<String, Object> getRedisCodec() {
        return redisCodec;
    }

    public CacheListener getCacheListener() {
        return cacheListener;
    }
}
