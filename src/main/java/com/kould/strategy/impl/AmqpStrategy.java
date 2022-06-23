package com.kould.strategy.impl;

import com.kould.entity.KacheMessage;
import com.kould.entity.MethodPoint;
import com.kould.strategy.Strategy;
import com.kould.utils.KryoUtil;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * AMQP异步删改缓存策略
 *
 * 异步更新处理器
 * 分布式处理需要通过广播实现各节点消息同时消费，且需保持增删改幂等性，防止重复删除
 *
 * 每个应用对会随机生成ID，以一个应用三个队列的形式绑定Kache_Exchange路由器
 * routingKey统一为增删改三种，增删改消息会复制给同一操作种类的所有队列消费
 */
public class AmqpStrategy extends Strategy {

    private static final String ID = "." + UUID.randomUUID();

    private static final String EXCHANGE_NAME = "Kache_Exchange";

    private static final String QUEUE_DELETE_NAME = "KACHE_DELETE_QUEUE" + ID;

    private static final String QUEUE_UPDATE_NAME = "KACHE_UPDATE_QUEUE" + ID;

    private static final String QUEUE_INSERT_NAME = "KACHE_INSERT_QUEUE" + ID;

    private static final String DELETE_ROUTING_KEY = "kache.delete";

    private static final String UPDATE_ROUTING_KEY = "kache.update";

    private static final String INSERT_ROUTING_KEY = "kache.insert";

    private static final String DELETE_CONSUMER_TAG = "Kache-Delete";

    private static final String UPDATE_CONSUMER_TAG = "Kache-Update";

    private static final String INSERT_CONSUMER_TAG = "Kache-Insert";

    private static final String EXCHANGE_TYPE = "direct";

    private final Connection connection;

    private Channel channel;

    public AmqpStrategy(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Object delete(MethodPoint point, KacheMessage serviceMessage) throws Exception {
        return publish(point, serviceMessage, DELETE_ROUTING_KEY);
    }

    @Override
    public Object update(MethodPoint point, KacheMessage serviceMessage) throws Exception {
        return publish(point, serviceMessage, UPDATE_ROUTING_KEY);
    }

    @Override
    public Object insert(MethodPoint point, KacheMessage serviceMessage) throws Exception {
        return publish(point, serviceMessage, INSERT_ROUTING_KEY);
    }

    private Object publish(MethodPoint point, KacheMessage serviceMessage,String routingKey) throws Exception {
        //先通过植入点的方法执行后查看是否会发生错误，以免误操作
        Object proceed = point.execute();
        if (serviceMessage != null) {
            channel.basicPublish(EXCHANGE_NAME, routingKey, null
                    , KryoUtil.writeToByteArray(serviceMessage));
        }
        return proceed ;
    }

    /**
     * 进行AMQP的初始化操作
     * 声明Exchange与Queue,Consumer并绑定
     * @throws IOException
     */
    public void init() throws IOException {
        // 创建Channel
        channel = connection.createChannel();
        // 创建Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE, true);
        // 创建三种队列并绑定Kache_Exchange
        channel.queueDeclare(QUEUE_DELETE_NAME, false, false, true, null);
        channel.queueBind(QUEUE_DELETE_NAME, EXCHANGE_NAME, DELETE_ROUTING_KEY);
        channel.queueDeclare(QUEUE_UPDATE_NAME, false, false, true, null);
        channel.queueBind(QUEUE_UPDATE_NAME, EXCHANGE_NAME, UPDATE_ROUTING_KEY);
        channel.queueDeclare(QUEUE_INSERT_NAME, false, false, true, null);
        channel.queueBind(QUEUE_INSERT_NAME, EXCHANGE_NAME, INSERT_ROUTING_KEY);
        // 在这个channel中绑定增删改操作的消费者
        channel.basicConsume(QUEUE_DELETE_NAME, false, DELETE_CONSUMER_TAG, new MessageConsumer(channel, baseCacheManager::deleteCache));
        channel.basicConsume(QUEUE_UPDATE_NAME, false, UPDATE_CONSUMER_TAG, new MessageConsumer(channel, baseCacheManager::updateCache));
        channel.basicConsume(QUEUE_INSERT_NAME, false, INSERT_CONSUMER_TAG, new MessageConsumer(channel, baseCacheManager::insertCache));
    }

    /**
     * AMQP的资源释放
     * @throws IOException
     * @throws TimeoutException
     */
    public void destroy() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    /**
     * 封装消费者操作
     *
     * 用于代码重用
     */
    static class MessageConsumer extends DefaultConsumer {

        private final com.kould.function.Consumer<KacheMessage> consumer;

        public MessageConsumer(Channel channel, com.kould.function.Consumer<KacheMessage> consumer) {
            super(channel);
            this.consumer = consumer;
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            long deliveryTag = envelope.getDeliveryTag();
            try {
                // 优先消费数据后再进行应答
                consumer.accept((KacheMessage) KryoUtil.readFromByteArray(body));
                this.getChannel().basicAck(deliveryTag, false);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
