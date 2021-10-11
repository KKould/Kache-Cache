package com.kould.handler;

import com.kould.bean.KacheConfig;
import com.kould.bean.Message;
import com.kould.lock.KacheLock;
import com.kould.manager.InterprocessCacheManager;
import com.kould.manager.RemoteCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;

import static com.kould.amqp.KacheQueue.INTERPROCESS_UPDATE_EXCHANGE_NAME;
import static com.kould.amqp.KacheQueue.QUEUE_UPDATE_CACHE;

@Component
@Slf4j
public class UpdateHandler {

    private static final String METHOD_GET_ID = "getId" ;

    @Autowired
    private KacheConfig kacheConfig ;

    @Autowired
    private KacheLock kacheLock ;

    @Autowired
    private RemoteCacheManager remoteCacheManager;

    @Autowired
    private InterprocessCacheManager interprocessCacheManager;

    @RabbitListener(queues = QUEUE_UPDATE_CACHE)
    public void asyncUpdateHandler(Message msg) {
        if (msg.getArg() == null) {
            return;
        }
        String lockKey = msg.getArg().getClass().getSimpleName();
        Lock writeLock = null ;
        try {
            writeLock = kacheLock.writeLock(lockKey);
            Method methodGetId = msg.getArg().getClass().getMethod(METHOD_GET_ID, null);
            remoteCacheManager.updateById(methodGetId.invoke(msg.getArg()).toString(),msg.getArg()) ;
            kacheLock.unLock(writeLock);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!kacheLock.isLock(writeLock)) {
                kacheLock.unLock(writeLock);
            }
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(), //注意这里不要定义队列名称,系统会随机产生
            exchange = @Exchange(value = INTERPROCESS_UPDATE_EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
    ))
    public void asyncInterprocessUpdateHandler(Message msg) {
        InterprocessCacheClear(msg, kacheLock, interprocessCacheManager);
    }

    static void InterprocessCacheClear(Message msg, KacheLock kacheLock, InterprocessCacheManager interprocessCacheManager) {
        if (msg.getArg() == null) {
            return;
        }
        String lockKey = msg.getArg().getClass().getSimpleName();
        Lock writeLock = null ;
        try {
            writeLock = kacheLock.writeLock(lockKey);
            interprocessCacheManager.clear(msg.getCacheClazz()); ;
            kacheLock.unLock(writeLock);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!kacheLock.isLock(writeLock)) {
                kacheLock.unLock(writeLock);
            }
        }
    }
}
