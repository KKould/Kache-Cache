package com.kould.handler.impl;

import com.kould.handler.AsyncHandler;
import com.kould.handler.StrategyHandler;
import com.kould.message.KacheMessage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/*
基于AMQP协议的异步处理器
删除/更新策略为：先通过数据库删除，随后发送消息异步进行缓存删除
缓存存活时间为：基本时间+随机区间时间
 */
public class AmqpAsyncHandler extends StrategyHandler implements AsyncHandler {

    private static final Logger log = LoggerFactory.getLogger(AmqpAsyncHandler.class) ;

    private static final AmqpAsyncHandler INSTANCE = new AmqpAsyncHandler() ;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private AmqpAsyncHandler() {}

    public static AmqpAsyncHandler getInstance() {
        return INSTANCE ;
    }

    public static final String QUEUE_DELETE_CACHE = "KACHE_CACHE_DELETE" ;

    public static final String QUEUE_UPDATE_CACHE = "KACHE_CACHE_UPDATE" ;

    public static final String QUEUE_INSERT_CACHE = "KACHE_CACHE_INSERT" ;

    public static final String INTERPROCESS_DELETE_EXCHANGE_NAME = "KACHE_INTERPROCESS_DELETE_EXCHANGE" ;

    public static final String INTERPROCESS_UPDATE_EXCHANGE_NAME = "KACHE_INTERPROCESS_UPDATE_EXCHANGE" ;

    public static final String INTERPROCESS_INSERT_EXCHANGE_NAME = "KACHE_INTERPROCESS_INSERT_EXCHANGE" ;

    @Bean
    public Queue asyncDeleteCacheQueue() {
        return new Queue(QUEUE_DELETE_CACHE);
    }

    @Bean
    public Queue asyncUpdateCacheQueue() {
        return new Queue(QUEUE_UPDATE_CACHE);
    }


    @Override
    public Object delete(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable {
        return asyncChange(point,QUEUE_DELETE_CACHE,INTERPROCESS_DELETE_EXCHANGE_NAME,serviceMessage) ;
    }

    @Override
    public Object update(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable {
        return asyncChange(point,QUEUE_UPDATE_CACHE,INTERPROCESS_UPDATE_EXCHANGE_NAME,serviceMessage) ;
    }

    @Override
    public Object insert(ProceedingJoinPoint point, KacheMessage serviceMessage) throws Throwable {
        return asyncChange(point,QUEUE_DELETE_CACHE,INTERPROCESS_DELETE_EXCHANGE_NAME,serviceMessage) ;
    }

    @Override
    public int getCacheTime() {
        return (int) (daoProperties.getBaseTime() + Math.random() * daoProperties.getRandomTime());
    }

    private Object asyncChange(ProceedingJoinPoint point, String queue, String exchange, KacheMessage serviceMessage) throws Throwable {
        //先通过植入点的方法执行后查看是否会发生错误，以免误操作
        Object proceed = point.proceed();
        if (serviceMessage != null) {
            serviceMessage.setMethod(null);
            amqpTemplate.convertAndSend(queue, serviceMessage);
            amqpTemplate.convertAndSend(exchange, "", serviceMessage);
        }
        return proceed ;
    }

    @Override
    @RabbitListener(queues = {QUEUE_DELETE_CACHE,QUEUE_UPDATE_CACHE})
    public void listen2DeleteRemote(KacheMessage msg) throws Exception {
        cacheLogic.deleteRemoteCache(msg);
    }

    @Override
    @RabbitListener(bindings = @QueueBinding(
            value = @org.springframework.amqp.rabbit.annotation.Queue(), //注意这里不要定义队列名称,系统会随机产生
            exchange = @Exchange(value = INTERPROCESS_DELETE_EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
    ))
    public void listen2DeleteInterprocess(KacheMessage msg) throws Exception {
        cacheLogic.deleteInterprocessCache(msg);
    }

    @Override
    @RabbitListener(queues = QUEUE_UPDATE_CACHE)
    public void listen2UpdateRemote(KacheMessage msg) throws Exception {
        cacheLogic.updateRemoteCache(msg);
    }

    @Override
    @RabbitListener(bindings = @QueueBinding(
            value = @org.springframework.amqp.rabbit.annotation.Queue(), //注意这里不要定义队列名称,系统会随机产生
            exchange = @Exchange(value = INTERPROCESS_UPDATE_EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
    ))
    public void listen2UpdateInterprocess(KacheMessage msg) throws Exception {
        cacheLogic.updateInterprocessCache(msg);
    }

    @Override
    @RabbitListener(queues = QUEUE_INSERT_CACHE)
    public void listen2InsertRemote(KacheMessage msg) throws Exception {
        cacheLogic.insertRemoteCache(msg);
    }

    @Override
    @RabbitListener(bindings = @QueueBinding(
            value = @org.springframework.amqp.rabbit.annotation.Queue(), //注意这里不要定义队列名称,系统会随机产生
            exchange = @Exchange(value = INTERPROCESS_INSERT_EXCHANGE_NAME,type = ExchangeTypes.FANOUT)
    ))
    public void listen2InsertInterprocess(KacheMessage msg) throws Exception {
        cacheLogic.insertInterprocessCache(msg);
    }
}