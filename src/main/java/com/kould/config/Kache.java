package com.kould.config;

import com.kould.aspect.DaoCacheAop;
import com.kould.core.CacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.endpoint.KacheEndpoint;
import com.kould.handler.StrategyHandler;
import com.kould.listener.CacheListener;
import com.kould.locator.DaoLocator;
import com.kould.lock.KacheLock;
import com.kould.logic.CacheLogic;
import com.kould.manager.IBaseCacheManager;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.message.KacheMessage;
import com.kould.service.RedisService;
import io.lettuce.core.codec.RedisCodec;

public class Kache {

    private DaoProperties daoProperties;

    private DataFieldProperties dataFieldProperties;

    private InterprocessCacheProperties interprocessCacheProperties;

    private ListenerProperties listenerProperties;

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_SELECT = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.select*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_INSERT = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.insert*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_DELETE = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.delete*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_UPDATE = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.update*(..))";

    public static final String MYBATIS_PLUS_MAPPER_SELECT_BY_ID = "selectById" ;

    public static final String SERVICE_BY_ID = "METHOD_SERVICE_BY_ID" ;

    public static final String SERVICE_BY_FIELD = "METHOD_SERVICE_BY_FIELD" ;

    public static final String NO_ID_TAG = "NO_ID-" ;

    public static final String CACHE_PREFIX = "KACHE:" ;

    private CacheEncoder cacheEncoder;

    private IBaseCacheManager iBaseCacheManager;

    private RemoteCacheManager remoteCacheManager;

    private RedisService redisService;

    private InterprocessCacheManager interprocessCacheManager;

    private KacheLock kacheLock;

    private StrategyHandler strategyHandler;

    private CacheLogic cacheLogic;

    private CacheListener cacheListener;

    private DaoCacheAop daoCacheAop;

    private DaoLocator daoLocator;

    private CacheHandler cacheHandler;

    private KacheEndpoint kacheEndpoint;

    private RedisCodec<String, Object> redisCodec;

    public static class Builder implements com.kould.type.Builder<Kache> {

        private CacheEncoder cacheEncoder;

        private IBaseCacheManager iBaseCacheManager;

        private RemoteCacheManager remoteCacheManager;

        private RedisService redisService;

        private InterprocessCacheManager interprocessCacheManager;

        private KacheLock kacheLock;

        private StrategyHandler strategyHandler;

        private CacheLogic cacheLogic;

        private CacheListener cacheListener;

        private DaoCacheAop daoCacheAop;

        private DaoLocator daoLocator;

        private CacheHandler cacheHandler;

        private KacheEndpoint kacheEndpoint;

        private RedisCodec<String, Object> redisCodec;

        private DaoProperties daoProperties;

        private DataFieldProperties dataFieldProperties;

        private InterprocessCacheProperties interprocessCacheProperties;

        private ListenerProperties listenerProperties;

        public Builder() { }

        public Kache.Builder cacheEncoder(CacheEncoder cacheEncoder) {
            this.cacheEncoder = cacheEncoder;
            return this ;
        }

        @Override
        public Kache build() {
            return new Kache(this) ;
        }

        public Builder(Builder builder) {
            this.cacheEncoder = builder.cacheEncoder;
            this.cacheHandler = builder.cacheHandler;
            this.cacheListener = builder.cacheListener;
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
            this.kacheEndpoint = builder.kacheEndpoint;
            this.redisCodec = builder.redisCodec;
            this.redisService = builder.redisService;
            this.remoteCacheManager = builder.remoteCacheManager;
            this.strategyHandler = builder.strategyHandler;
        }
    }
}
