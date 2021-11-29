package com.kould.handler.impl;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.kould.handler.impl.AmqpAsyncHandler.*;

@Configuration
public class AmqpConfig {
    @Bean
    public Queue asyncDeleteCacheQueue() {
        return new Queue(QUEUE_DELETE_CACHE);
    }

    @Bean
    public Queue asyncUpdateCacheQueue() {
        return new Queue(QUEUE_UPDATE_CACHE);
    }

    @Bean
    public Queue asyncInsertCacheQueue() {
        return new Queue(QUEUE_INSERT_CACHE);
    }
}
