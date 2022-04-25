package com.kould.config;

import com.kould.aspect.DaoCacheAop;
import com.kould.codec.KryoRedisCodec;
import com.kould.core.CacheHandler;
import com.kould.core.impl.BaseCacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.encoder.impl.BaseCacheEncoder;
import com.kould.handler.StrategyHandler;
import com.kould.handler.impl.DBFirstHandler;
import com.kould.listener.CacheListener;
import com.kould.listener.impl.StatisticsListener;
import com.kould.locator.DaoLocator;
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

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Kache {

    private static Kache kache;

    private DaoProperties daoProperties;

    private DataFieldProperties dataFieldProperties;

    private InterprocessCacheProperties interprocessCacheProperties;

    private ListenerProperties listenerProperties;

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_SELECT = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.select*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_INSERT = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.insert*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_DELETE = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.delete*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_UPDATE = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.update*(..))";

    public static final String MYBATIS_PLUS_MAPPER_SELECT_BY_ID = "selectById" ;

    //"METHOD_SERVICE_BY_ID "
    public static final String SERVICE_BY_ID = "MSBI" ;

    //"METHOD_SERVICE_BY_FIELD"
    public static final String SERVICE_BY_FIELD = "MSBF" ;

    //"NO_ID-"
    public static final String NO_ID_TAG = "NI-" ;

    public static final String CACHE_PREFIX = "KACHE:" ;

    private CacheEncoder cacheEncoder;

    private IBaseCacheManager iBaseCacheManager;

    private RemoteCacheManager remoteCacheManager;

    private RedisService redisService;

    private InterprocessCacheManager interprocessCacheManager;

    private KacheLock kacheLock;

    private StrategyHandler strategyHandler;

    private CacheLogic cacheLogic;

    private DaoCacheAop daoCacheAop;

    private DaoLocator daoLocator;

    private CacheHandler cacheHandler;

    private RedisCodec<String, Object> redisCodec;

    private CacheListener cacheListener = StatisticsListener.newInstance();

    public static class Builder implements com.kould.type.Builder<Kache> {

        private RedisClient redisClient;

        private CacheEncoder cacheEncoder;

        private KacheLock kacheLock;

        private CacheHandler cacheHandler;

        private RedisCodec<String, Object> redisCodec;

        private DaoProperties daoProperties;

        private DataFieldProperties dataFieldProperties;

        private InterprocessCacheProperties interprocessCacheProperties;

        private ListenerProperties listenerProperties;

        private DaoLocator daoLocator;

        private InterprocessCacheManager interprocessCacheManager;

        private RemoteCacheManager remoteCacheManager;

        private CacheLogic cacheLogic;

        private StrategyHandler strategyHandler;

        private IBaseCacheManager iBaseCacheManager;

        private DaoCacheAop daoCacheAop;

        private RedisService redisService;

        public Builder() { }

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

        public Kache.Builder daoCacheAop(DaoCacheAop daoCacheAop) {
            this.daoCacheAop = daoCacheAop;
            return this;
        }

        public Kache.Builder daoLocator(DaoLocator daoLocator) {
            this.daoLocator = daoLocator;
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
            if (this.daoLocator == null) {
                this.daoLocator = new DaoLocator(daoProperties);
            }
            if (this.interprocessCacheManager == null) {
                this.interprocessCacheManager = new GuavaCacheManager(daoProperties
                        , interprocessCacheProperties);
            }
            if (this.remoteCacheManager == null) {
                this.remoteCacheManager = new RedisCacheManager(dataFieldProperties, daoProperties);
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
            if (this.daoCacheAop == null) {
                this.daoCacheAop = new DaoCacheAop(iBaseCacheManager, strategyHandler, listenerProperties
                        , cacheHandler, cacheEncoder);
            }
            if (this.redisService == null) {
                this.redisService = new RedisService(daoProperties, redisClient, redisCodec);
            }
            kache = new Kache(this);
            return kache ;
        }
    }

    public Kache(Builder builder) {
        this.cacheEncoder = builder.cacheEncoder;
        this.cacheHandler = builder.cacheHandler;
        this.cacheLogic = builder.cacheLogic;
        this.daoCacheAop = builder.daoCacheAop;
        this.daoProperties = builder.daoProperties;
        this.daoLocator = builder.daoLocator;
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

    public static Kache getInstance() {
        return kache;
    }

    public DaoProperties getDaoProperties() {
        return daoProperties;
    }

    public void setDaoProperties(DaoProperties daoProperties) {
        this.daoProperties = daoProperties;
    }

    public DataFieldProperties getDataFieldProperties() {
        return dataFieldProperties;
    }

    public void setDataFieldProperties(DataFieldProperties dataFieldProperties) {
        this.dataFieldProperties = dataFieldProperties;
    }

    public InterprocessCacheProperties getInterprocessCacheProperties() {
        return interprocessCacheProperties;
    }

    public void setInterprocessCacheProperties(InterprocessCacheProperties interprocessCacheProperties) {
        this.interprocessCacheProperties = interprocessCacheProperties;
    }

    public ListenerProperties getListenerProperties() {
        return listenerProperties;
    }

    public void setListenerProperties(ListenerProperties listenerProperties) {
        this.listenerProperties = listenerProperties;
    }

    public CacheEncoder getCacheEncoder() {
        return cacheEncoder;
    }

    public void setCacheEncoder(CacheEncoder cacheEncoder) {
        this.cacheEncoder = cacheEncoder;
    }

    public IBaseCacheManager getiBaseCacheManager() {
        return iBaseCacheManager;
    }

    public void setiBaseCacheManager(IBaseCacheManager iBaseCacheManager) {
        this.iBaseCacheManager = iBaseCacheManager;
    }

    public RemoteCacheManager getRemoteCacheManager() {
        return remoteCacheManager;
    }

    public void setRemoteCacheManager(RemoteCacheManager remoteCacheManager) {
        this.remoteCacheManager = remoteCacheManager;
    }

    public RedisService getRedisService() {
        return redisService;
    }

    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    public InterprocessCacheManager getInterprocessCacheManager() {
        return interprocessCacheManager;
    }

    public void setInterprocessCacheManager(InterprocessCacheManager interprocessCacheManager) {
        this.interprocessCacheManager = interprocessCacheManager;
    }

    public KacheLock getKacheLock() {
        return kacheLock;
    }

    public void setKacheLock(KacheLock kacheLock) {
        this.kacheLock = kacheLock;
    }

    public StrategyHandler getStrategyHandler() {
        return strategyHandler;
    }

    public void setStrategyHandler(StrategyHandler strategyHandler) {
        this.strategyHandler = strategyHandler;
    }

    public CacheLogic getCacheLogic() {
        return cacheLogic;
    }

    public void setCacheLogic(CacheLogic cacheLogic) {
        this.cacheLogic = cacheLogic;
    }

    public DaoCacheAop getDaoCacheAop() {
        return daoCacheAop;
    }

    public void setDaoCacheAop(DaoCacheAop daoCacheAop) {
        this.daoCacheAop = daoCacheAop;
    }

    public DaoLocator getDaoLocator() {
        return daoLocator;
    }

    public void setDaoLocator(DaoLocator daoLocator) {
        this.daoLocator = daoLocator;
    }

    public CacheHandler getCacheHandler() {
        return cacheHandler;
    }

    public void setCacheHandler(CacheHandler cacheHandler) {
        this.cacheHandler = cacheHandler;
    }

    public RedisCodec<String, Object> getRedisCodec() {
        return redisCodec;
    }

    public void setRedisCodec(RedisCodec<String, Object> redisCodec) {
        this.redisCodec = redisCodec;
    }

    public CacheListener getCacheListener() {
        return cacheListener;
    }

    public void setCacheListener(CacheListener cacheListener) {
        this.cacheListener = cacheListener;
    }
}
