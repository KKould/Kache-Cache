package com.kould.config;

import com.kould.aspect.DaoCacheAop;
import com.kould.codec.KryoRedisCodec;
import com.kould.core.CacheHandler;
import com.kould.core.impl.BaseCacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.encoder.impl.BaseCacheEncoder;
import com.kould.endpoint.KacheEndpoint;
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
import io.lettuce.core.codec.RedisCodec;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        DaoProperties.class,
        DataFieldProperties.class,
        InterprocessCacheProperties.class,
        ListenerProperties.class
})
public class KacheAutoConfig {

    public KacheAutoConfig() {
    }

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_SELECT = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.select*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_INSERT = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.insert*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_DELETE = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.delete*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_MYBATIS_PLUS_UPDATE = "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.update*(..))";

    public static final String MYBATIS_PLUS_MAPPER_SELECT_BY_ID = "selectById" ;

    public static final String SERVICE_BY_ID = "METHOD_SERVICE_BY_ID" ;

    public static final String SERVICE_BY_FIELD = "METHOD_SERVICE_BY_FIELD" ;

    public static final String NO_ID_TAG = "NO_ID-" ;

    public static final String CACHE_PREFIX = "KACHE:" ;

    @Bean
    @ConditionalOnMissingBean
    public CacheEncoder cacheEncoder() {
        return BaseCacheEncoder.getInstance() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public IBaseCacheManager iBaseCacheManager() {
        return BaseCacheManagerImpl.getInstance() ;
    }

    @Bean("KacheRedisManager")
    @ConditionalOnMissingBean
    public RemoteCacheManager remoteCacheManager() {
        return RedisCacheManager.getInstance();
    }

    @Bean
    @ConditionalOnClass(RedisCacheManager.class)
    public RedisService redisService() {
        return new RedisService();
    }

    @Bean
    @ConditionalOnMissingBean
    public InterprocessCacheManager interprocessCacheManager() {
        return GuavaCacheManager.getInstance() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public KacheLock kacheLock() {
        return LocalLock.getInstance() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public StrategyHandler strategyHandler() {
        return new DBFirstHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheLogic cacheLogic() {
        return BaseCacheLogic.getInstance();
    }

    @Bean
    public CacheListener cacheListener() {
        return StatisticsListener.newInstance() ;
    }

    @Bean
    public DaoCacheAop daoCacheAop() {
        return new DaoCacheAop() ;
    }

    @Bean
    public DaoLocator daoLocator() {
        return new DaoLocator();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheHandler cacheHandler() {
        return new BaseCacheHandler() ;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public KacheEndpoint kacheEndpoint() {
        return new KacheEndpoint();
    }

    @Bean("KacheRedisCodec")
    @ConditionalOnMissingBean(name = "KacheRedisCodec")
    public RedisCodec<String, Object> redisCodec() {
        return new KryoRedisCodec();
    }
}
