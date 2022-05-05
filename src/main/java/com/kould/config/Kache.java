package com.kould.config;

import interceptor.CacheMethodInterceptor;
import com.kould.codec.KryoRedisCodec;
import com.kould.core.CacheHandler;
import com.kould.core.impl.BaseCacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.encoder.impl.BaseCacheEncoder;
import com.kould.entity.RegexEntity;
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

    //"METHOD_SERVICE_BY_ID "
    public static final String SERVICE_BY_ID = "$MSBI" ;

    //"METHOD_SERVICE_BY_FIELD"
    public static final String SERVICE_BY_FIELD = "$MSBF" ;

    //"NO_ID-"
    public static final String NO_ID_TAG = "$NI-" ;

    public static final String MAPPER_PATH_NULL = "Kache's mapperPackage is null!";

    public static final String CACHE_PREFIX = "KACHE:" ;

    private final String mapperPackage;

    private final String selectRegex;

    private final String insertRegex;

    private final String deleteRegex;

    private final String updateRegex;

    private final String selectStatusByIdRegex;

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

        private String selectRegex;

        private String insertRegex;

        private String deleteRegex;

        private String updateRegex;

        private String selectStatusByIdRegex;

        private String mapperPackage;

        private RedisClient redisClient;

        private CacheEncoder cacheEncoder;

        private KacheLock kacheLock;

        private CacheHandler cacheHandler;

        private RedisCodec<String, Object> redisCodec;

        private DaoProperties daoProperties;

        private DataFieldProperties dataFieldProperties;

        private InterprocessCacheProperties interprocessCacheProperties;

        private ListenerProperties listenerProperties;

        private InterprocessCacheManager interprocessCacheManager;

        private RemoteCacheManager remoteCacheManager;

        private CacheLogic cacheLogic;

        private StrategyHandler strategyHandler;

        private IBaseCacheManager iBaseCacheManager;

        private RedisService redisService;

        public Builder() { }

        public Kache.Builder selectRegex(String selectRegex) {
            this.selectRegex = selectRegex;
            return this;
        }

        public Kache.Builder insertRegex(String insertRegex) {
            this.insertRegex = insertRegex;
            return this;
        }

        public Kache.Builder deleteRegex(String deleteRegex) {
            this.deleteRegex = deleteRegex;
            return this;
        }

        public Kache.Builder updateRegex(String updateRegex) {
            this.updateRegex = updateRegex;
            return this;
        }

        public Kache.Builder selectStatusByIdRegex(String selectStatusByIdRegex) {
            this.selectStatusByIdRegex = selectStatusByIdRegex;
            return this;
        }

        public Kache.Builder mapperPackage(String mapperPackage) {
            this.mapperPackage = mapperPackage;
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
            if (this.selectRegex == null) {
                this.selectRegex = "^select.*";
            }
            if (this.insertRegex == null) {
                this.insertRegex = "^insert.*";
            }
            if (this.deleteRegex == null) {
                this.deleteRegex = "^delete.*";
            }
            if (this.updateRegex == null) {
                this.updateRegex = "^update.*";
            }
            if (this.selectStatusByIdRegex == null) {
                this.selectStatusByIdRegex = "selectById";
            }
            if (this.mapperPackage == null) {
                throw new NullPointerException(MAPPER_PATH_NULL);
            }
            if (this.redisClient == null) {
                RedisURI redisUri = RedisURI.builder()                    // <1> 建立单机链接的链接信息
                        .withHost("localhost")
                        .withPort(6379)
                        .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                        .build();
                this.redisClient = RedisClient.create(redisUri);
            }
            if (this.cacheEncoder == null) {
                this.cacheEncoder = BaseCacheEncoder.getInstance();
            }
            if (this.kacheLock == null) {
                this.kacheLock = new LocalLock();
            }
            if (this.cacheHandler == null) {
                this.cacheHandler = new BaseCacheHandler();
            }
            if (this.redisCodec == null) {
                this.redisCodec = new KryoRedisCodec();
            }
            if (this.daoProperties == null) {
                this.daoProperties = new DaoProperties();
            }
            if (this.dataFieldProperties == null) {
                this.dataFieldProperties = new DataFieldProperties();
            }
            if (this.interprocessCacheProperties == null) {
                this.interprocessCacheProperties = new InterprocessCacheProperties();
            }
            if (this.listenerProperties == null) {
                this.listenerProperties = new ListenerProperties();
            }
            if (this.interprocessCacheManager == null) {
                this.interprocessCacheManager = new GuavaCacheManager(daoProperties
                        , interprocessCacheProperties);
            }
            if (this.redisService == null) {
                this.redisService = new RedisService(daoProperties, redisClient, redisCodec);
            }
            if (this.remoteCacheManager == null) {
                this.remoteCacheManager = new RedisCacheManager(dataFieldProperties, daoProperties
                        , redisService, kacheLock);
            }
            if (this.cacheLogic == null) {
                this.cacheLogic = new BaseCacheLogic(kacheLock, cacheEncoder, remoteCacheManager
                        , interprocessCacheManager);
            }
            if (this.strategyHandler == null) {
                this.strategyHandler = new DBFirstHandler(daoProperties, cacheLogic);
            }
            if (this.iBaseCacheManager == null) {
                this.iBaseCacheManager = new BaseCacheManagerImpl(interprocessCacheManager
                        , remoteCacheManager, interprocessCacheProperties);
            }
            return new Kache(this) ;
        }
    }

    public static Builder builder() {
        return new Builder() ;
    }

    public Kache(Builder builder) {
        this.selectRegex = builder.selectRegex;
        this.insertRegex = builder.insertRegex;
        this.deleteRegex = builder.deleteRegex;
        this.updateRegex = builder.updateRegex;
        this.selectStatusByIdRegex = builder.selectStatusByIdRegex;
        this.mapperPackage = builder.mapperPackage;
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
                , new RegexEntity(selectRegex ,insertRegex ,deleteRegex ,updateRegex, selectStatusByIdRegex)));
    }

    public void init() throws Exception {
        remoteCacheManager.init();
    }

    public void destroy() throws Exception{
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

    public String getMapperPackage() {
        return mapperPackage;
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
