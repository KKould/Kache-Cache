package com.kould.handler;

import com.kould.bean.KacheConfig;
import com.kould.bean.Message;
import com.kould.encoder.CacheEncoder;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import static com.kould.amqp.KacheQueue.*;

@Component
@Slf4j
public class DeleteHandler {

    @Autowired
    private KacheConfig kacheConfig ;

    @Autowired
    private KacheLock kacheLock ;

    @Autowired
    private CacheEncoder cacheEncoder ;

    @Autowired
    private  RemoteCacheManager remoteCacheManager;

    @Autowired
    private InterprocessCacheManager interprocessCacheManager ;

    @RabbitListener(queues = QUEUE_DELETE_CACHE)
    public void asyncDeleteHandler(Message msg) {
        if (msg.getArg() == null) {
            return;
        }
        Class resultClass = msg.getCacheClazz();
        String lockKey = msg.getArg().getClass().getSimpleName();
        Lock writeLock = null;
        try {
            writeLock = kacheLock.writeLock(lockKey);
            log.info("Kache:+++++++++Redis缓存删除检测");
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
            kacheLock.unLock(writeLock);
        } finally {
            if (!kacheLock.isLock(writeLock)) {
                kacheLock.unLock(writeLock);
            }
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(), //注意这里不要定义队列名称,系统会随机产生
            exchange = @Exchange(value = INTERPROCESS_DELETE_EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
    ))
    public void asyncInterprocessDeleteHandler(Message msg) {
        UpdateHandler.InterprocessCacheClear(msg, kacheLock, interprocessCacheManager);
    }
}
