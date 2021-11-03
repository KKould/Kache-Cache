package com.kould.config;

import com.kould.encoder.CacheEncoder;
import com.kould.encoder.impl.BaseCacheEncoder;
import com.kould.json.GsonUtil;
import com.kould.json.JsonUtil;
import com.kould.lock.KacheLock;
import com.kould.lock.RedissonLock;
import com.kould.manager.IBaseCacheManager;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import com.kould.manager.impl.BaseCacheManagerImpl;
import com.kould.manager.impl.GuavaCacheManager;
import com.kould.manager.impl.RedisCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        DaoProperties.class,
        DataFieldProperties.class,
        InterprocessCacheProperties.class
})
public class KacheAutoConfig {

    public KacheAutoConfig() {
    }

    public static final String POINTCUT_EXPRESSION_DAO_FIND = "execution(* *.*.mapper..*.select*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_ADD = "execution(* *.*.mapper..*.insert*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_REMOVE = "execution(* *.*.mapper..*.delete*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_EDIT = "execution(* *.*.mapper..*.update*(..))";

    public static final String POINTCUT_EXPRESSION_SERVICE = "execution(* *.*.service..*.*(..))";

    public static final String SERVICE_LIKE = "KACHE_SERVICE_LIKE" ;

    public static final String SERVICE_IS = "KACHE_SERVICE_IS" ;

    public static final String SERVICE_BY_ID = "KACHE_SERVICE_BY_ID" ;

    public static final String SERVICE_NO_ARG = "KACHE_SERVICE_NOARG" ;

    public static final String SERVICE_ALL = "KACHE_SERVICE_ALL" ;

    public static final String NO_ID_TAG = "NO_ID_" ;

    @Bean
    @ConditionalOnMissingBean
    public CacheEncoder cacheEncoder() {
        return new BaseCacheEncoder() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public IBaseCacheManager iBaseCacheManager() {
        return new BaseCacheManagerImpl() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public RemoteCacheManager remoteCacheManager() {
        return new RedisCacheManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public InterprocessCacheManager interprocessCacheManager() {
        return new GuavaCacheManager() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonUtil jsonUtil() {
        return new GsonUtil() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public KacheLock kacheLock() {
        return new RedissonLock() ;
    }
}
