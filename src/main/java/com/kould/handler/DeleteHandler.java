package com.kould.handler;

import com.kould.annotation.CacheBeanClass;
import com.kould.bean.KacheConfig;
import com.kould.bean.Message;
import com.kould.encoder.CacheEncoder;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.kould.amqp.KacheQueue.*;

@Component
@Slf4j
public class DeleteHandler {

    @Autowired
    private KacheConfig kacheConfig ;

    @Autowired
    private CacheEncoder cacheEncoder ;

    @Autowired
    private  RemoteCacheManager remoteCacheManager;

    @Autowired
    private InterprocessCacheManager interprocessCacheManager ;

    @Autowired
    private RedissonClient redissonClient;

    @RabbitListener(queues = QUEUE_DELETE_CACHE)
    public void asyncDeleteHandler(Message msg) {
        if (msg.getArg() == null) {
            return;
        }
        //需要去获取这个dao代表的po类
        CacheBeanClass cacheBeanClass = (CacheBeanClass) msg.getClazz().getAnnotation(CacheBeanClass.class);
        Class resultClass = cacheBeanClass.clazz();
        String lockKey = msg.getArg().getClass().getSimpleName();
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);
        RLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock(kacheConfig.getLockTime(), TimeUnit.SECONDS);
            log.info("+++++++++Redis缓存删除检测");
            Map<String, String> args = cacheEncoder.section2Field(msg.getArg(), msg.getMethodName());
            List<String> allKey = remoteCacheManager.keys(cacheEncoder.getPattern(resultClass.getName()));
            List<String> delKeys = new ArrayList<>();
            allKey.parallelStream().forEach(key -> {
                Map<String, String> keySection = cacheEncoder.section2Field(cacheEncoder.decode(key, HashMap.class, resultClass.getName()), msg.getMethodName());
                if (key.contains(KacheConfig.SERVICE_NOARG) || key.contains(KacheConfig.SERVICE_ALL)) {
                    delKeys.add(key);
                } else if (key.contains(KacheConfig.SERVICE_LIKE)) {
                    keySection.keySet().parallelStream().forEach(field -> {
                        String KeyField = keySection.get(field);
                        String argField = args.get(field);
                        if (KeyField != null && argField != null) {
                            if (argField.contains(KeyField)) {
                                delKeys.add(key);
                                return;
                            }
                        }
                    });
                } else {
                    keySection.keySet().parallelStream().forEach(field -> {
                        String KeyField = keySection.get(field);
                        String argField = args.get(field);
                        if (KeyField != null && argField != null) {
                            if (argField.equals(KeyField)) {
                                delKeys.add(key);
                                return;
                            }
                        }
                    });
                }
            });
            if (delKeys.size() > 0) {
                remoteCacheManager.del(delKeys.toArray(new String[delKeys.size()]));
            }
            writeLock.unlock();
        } finally {
            if (writeLock.isLocked() && writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(), //注意这里不要定义队列名称,系统会随机产生
            exchange = @Exchange(value = INTERPROCESS_DELETE_EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
    ))
    public void asyncInterprocessDeleteHandler(Message msg) {
        if (msg.getArg() == null) {
            return;
        }
        String lockKey = msg.getArg().getClass().getSimpleName();
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);
        RLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock(kacheConfig.getLockTime(), TimeUnit.SECONDS);
            interprocessCacheManager.clear(); ;
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
