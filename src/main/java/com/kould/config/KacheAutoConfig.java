package com.kould.config;

import com.kould.aspect.DaoCacheAop;
import com.kould.aspect.ServiceMessageAop;
import com.kould.encoder.CacheEncoder;
import com.kould.encoder.impl.BaseCacheEncoder;
import com.kould.handler.StrategyHandler;
import com.kould.handler.impl.AmqpAsyncHandler;
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
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.kould.handler.impl.AmqpAsyncHandler.*;

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

    public static final String POINTCUT_EXPRESSION_SERVICE_MYBATIS_PLUS_ISERVICE = "execution(* com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.*(..))" ;

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
    @ConditionalOnMissingBean
    public Queue asyncDeleteCacheQueue() {
        return new Queue(QUEUE_DELETE_CACHE);
    }

    @Bean
    @ConditionalOnMissingBean
    public Queue asyncUpdateCacheQueue() {
        return new Queue(QUEUE_UPDATE_CACHE);
    }

    @Bean
    @ConditionalOnMissingBean
    public Queue asyncInsertCacheQueue() {
        return new Queue(QUEUE_INSERT_CACHE);
    }

    @Bean
    public ServiceMessageAop serviceMessageAop() {
        return new ServiceMessageAop() ;
    }

    @Bean
    public DaoCacheAop daoCacheAop() {
        return new DaoCacheAop() ;
    }
}
