package com.kould.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisAutoConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private Integer port;
    //    @Value("${spring.redis.password}")
//    private String password;
    @Value("${spring.redis.database}")
    private Integer database;

    @Bean
    RedisClient redisClient() {
        RedisURI uri = RedisURI.Builder.redis(this.host, this.port)
//                .withPassword(this.password)
                .withDatabase(this.database)
                .build();
        return RedisClient.create(uri);
    }
}
