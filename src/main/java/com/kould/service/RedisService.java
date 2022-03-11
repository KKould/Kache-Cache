package com.kould.service;

import com.kould.codec.KryoRedisCodec;
import com.kould.config.DaoProperties;
import com.kould.function.SyncCommandCallback;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class RedisService {

    @Autowired
    private DaoProperties daoProperties;

    @Autowired
    RedisClient redisClient;

    GenericObjectPool<StatefulRedisConnection<String, Object>> redisConnectionPool;

    @PostConstruct
    public void init() {
        GenericObjectPoolConfig<StatefulRedisConnection<String, Object>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(daoProperties.getPoolMaxTotal());
        poolConfig.setMaxIdle(daoProperties.getPoolMaxIdle());
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        this.redisConnectionPool = ConnectionPoolSupport.createGenericObjectPool(() -> redisClient.connect(new KryoRedisCodec()), poolConfig);
    }

    @PreDestroy
    public void shutdown() {
        this.redisConnectionPool.close();
        this.redisClient.shutdown();
    }

    public <T> T executeSync(SyncCommandCallback<T> callback) throws Throwable {
        try (StatefulRedisConnection<String, Object> connection = redisConnectionPool.borrowObject()) {
            connection.setAutoFlushCommands(true);
            RedisCommands<String, Object> commands = connection.sync();
            return callback.doInConnection(commands);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
