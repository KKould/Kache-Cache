package com.kould.handler;

import com.kould.bean.KacheConfig;
import com.kould.bean.Message;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static com.kould.amqp.KacheQueue.INTERPROCESS_UPDATE_EXCHANGE_NAME;
import static com.kould.amqp.KacheQueue.QUEUE_UPDATE_CACHE;

@Component
@Slf4j
public class UpdateHandler {

    private static final String METHOD_GET_ID = "getId" ;

    @Autowired
    private KacheConfig kacheConfig ;

    @Autowired
    private RemoteCacheManager remoteCacheManager;

    @Autowired
    private InterprocessCacheManager interprocessCacheManager;

    @Autowired
    private RedissonClient redissonClient;

    @RabbitListener(queues = QUEUE_UPDATE_CACHE)
    public void asyncUpdateHandler(Message msg) {
        if (msg.getArg() == null) {
            return;
        }
        String lockKey = msg.getArg().getClass().getSimpleName();
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);
        RLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock(kacheConfig.getLockTime(), TimeUnit.SECONDS);
            Method methodGetId = msg.getArg().getClass().getMethod(METHOD_GET_ID, null);
            remoteCacheManager.updateById(methodGetId.invoke(msg.getArg()).toString(),msg.getArg()) ;
            writeLock.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writeLock.isLocked() && writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(), //注意这里不要定义队列名称,系统会随机产生
            exchange = @Exchange(value = INTERPROCESS_UPDATE_EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
    ))
    public void asyncInterprocessUpdateHandler(Message msg) {
        if (msg.getArg() == null) {
            return;
        }
        String lockKey = msg.getArg().getClass().getSimpleName();
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);
        RLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock(kacheConfig.getLockTime(), TimeUnit.SECONDS);
            interprocessCacheManager.clear(msg.getCacheClazz()); ;
            writeLock.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writeLock.isLocked() && writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
    }
}
