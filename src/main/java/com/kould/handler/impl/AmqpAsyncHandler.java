package com.kould.handler.impl;

import com.kould.manager.IBaseCacheManager;
import com.kould.properties.DaoProperties;
import com.kould.handler.AsyncHandler;
import com.kould.entity.KacheMessage;
import com.kould.entity.MethodPoint;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/*
基于AMQP协议的异步处理器
删除/更新策略为：先通过数据库删除，随后发送消息异步进行缓存删除
缓存存活时间为：基本时间+随机区间时间
 */
public class AmqpAsyncHandler extends AsyncHandler {

    private final AmqpTemplate amqpTemplate;

    public AmqpAsyncHandler(DaoProperties daoProperties, IBaseCacheManager baseCacheManager, AmqpTemplate amqpTemplate) {
        super(daoProperties, baseCacheManager);
        this.amqpTemplate = amqpTemplate;
    }

    public static final String QUEUE_DELETE_CACHE = "KACHE_CACHE_DELETE" ;

    public static final String QUEUE_UPDATE_CACHE = "KACHE_CACHE_UPDATE" ;

    public static final String QUEUE_INSERT_CACHE = "KACHE_CACHE_INSERT" ;

    @Override
    public Object delete(MethodPoint point, KacheMessage serviceMessage) throws Exception {
        return asyncChange(point,QUEUE_DELETE_CACHE,serviceMessage) ;
    }

    @Override
    public Object update(MethodPoint point, KacheMessage serviceMessage) throws Exception {
        return asyncChange(point,QUEUE_UPDATE_CACHE,serviceMessage) ;
    }

    @Override
    public Object insert(MethodPoint point, KacheMessage serviceMessage) throws Exception {
        return asyncChange(point,QUEUE_INSERT_CACHE,serviceMessage) ;
    }

    private Object asyncChange(MethodPoint point, String queue, KacheMessage serviceMessage) throws Exception {
        //先通过植入点的方法执行后查看是否会发生错误，以免误操作
        Object proceed = point.execute();
        if (serviceMessage != null) {
            amqpTemplate.convertAndSend(queue, serviceMessage);
        }
        return proceed ;
    }

    @Override
    @RabbitListener(queues = {QUEUE_DELETE_CACHE})
    public void listen2Delete(KacheMessage msg) throws Exception {
        baseCacheManager.deleteCache(msg);
    }

    @Override
    @RabbitListener(queues = QUEUE_UPDATE_CACHE)
    public void listen2Update(KacheMessage msg) throws Exception {
        baseCacheManager.updateCache(msg);
    }

    @Override
    @RabbitListener(queues = QUEUE_INSERT_CACHE)
    public void listen2Insert(KacheMessage msg) throws Exception {
        baseCacheManager.insertCache(msg);
    }

}
