package com.kould.service;

import com.kould.properties.DaoProperties;
import com.kould.function.SyncCommandCallback;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RedisService {

    private final RedisClient redisClient;

    GenericObjectPool<StatefulRedisConnection<String, Object>> redisConnectionPool;

    public RedisService(DaoProperties daoProperties, RedisClient redisClient, RedisCodec<String, Object> redisCodec) {
        this.redisClient = redisClient;
        GenericObjectPoolConfig<StatefulRedisConnection<String, Object>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(daoProperties.getPoolMaxTotal());
        poolConfig.setMaxIdle(daoProperties.getPoolMaxIdle());
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        this.redisConnectionPool = ConnectionPoolSupport.createGenericObjectPool(() -> redisClient.connect(redisCodec)
                , poolConfig);
    }

    public void shutdown() {
        this.redisConnectionPool.close();
        this.redisClient.shutdown();
    }

    public <T> T executeSync(SyncCommandCallback<T> callback) throws Exception {
        try (StatefulRedisConnection<String, Object> connection = redisConnectionPool.borrowObject()) {
            connection.setAutoFlushCommands(true);
            RedisCommands<String, Object> commands = connection.sync();
            return callback.doInConnection(commands);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
