package com.kould.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KacheQueue {

    public static final String QUEUE_DELETE_CACHE = "KACHE_CACHE_DELETE" ;

    public static final String QUEUE_UPDATE_CACHE = "KACHE_CACHE_UPDATE" ;

    public static final String INTERPROCESS_DELETE_EXCHANGE_NAME = "KACHE_INTERPROCESS_DELETE_EXCHANGE" ;

    public static final String INTERPROCESS_UPDATE_EXCHANGE_NAME = "KACHE_INTERPROCESS_UPDATE_EXCHANGE" ;

    @Bean
    public org.springframework.amqp.core.Queue asyncDeleteCache() {
        return new Queue(QUEUE_DELETE_CACHE);
    }

    @Bean
    public org.springframework.amqp.core.Queue asyncUpdateCache() {
        return new Queue(QUEUE_UPDATE_CACHE);
    }
}
