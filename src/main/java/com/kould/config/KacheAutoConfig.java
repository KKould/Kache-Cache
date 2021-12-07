package com.kould.config;

import com.kould.encoder.CacheEncoder;
import com.kould.encoder.impl.BaseCacheEncoder;
import com.kould.handler.StrategyHandler;
import com.kould.handler.impl.AmqpAsyncHandler;
import com.kould.json.GsonUtil;
import com.kould.json.JsonUtil;
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
import com.kould.serializer.KryoSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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

    public static final String SERVICE_LIKE = "METHOD_SERVICE_LIKE" ;

    public static final String SERVICE_IS = "METHOD_SERVICE_IS" ;

    public static final String SERVICE_BY_ID = "METHOD_SERVICE_BY_ID" ;

    public static final String SERVICE_NO_ARG = "METHOD_SERVICE_NO_ARG" ;

    public static final String SERVICE_ALL = "METHOD_SERVICE_ALL" ;

    public static final String NO_ID_TAG = "NO_ID_" ;

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

    @Bean
    @ConditionalOnMissingBean
    public RemoteCacheManager remoteCacheManager() {
        return RedisCacheManager.getInstance();
    }

    @Bean
    @ConditionalOnMissingBean
    public InterprocessCacheManager interprocessCacheManager() {
        return GuavaCacheManager.getInstance() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonUtil jsonUtil() {
        return GsonUtil.getInstance() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public KacheLock kacheLock() {
        return LocalLock.getInstance() ;
    }

    @Bean
    @ConditionalOnMissingBean
    public StrategyHandler strategyHandler() {
        return AmqpAsyncHandler.getInstance();
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
    public RedisTemplate<String, Object> getRedisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<String, Object> rt = new RedisTemplate<>();
        rt.setConnectionFactory(connectionFactory);
        rt.setKeySerializer(new StringRedisSerializer());
        rt.setValueSerializer(new KryoSerializer());
        return rt;
    }
}
