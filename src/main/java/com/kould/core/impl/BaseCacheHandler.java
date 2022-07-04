package com.kould.core.impl;

import com.kould.api.Kache;
import com.kould.core.CacheHandler;
import com.kould.entity.Status;
import com.kould.entity.NullValue;
import com.kould.listener.ListenerHandler;
import com.kould.entity.MethodPoint;

import java.lang.reflect.Method;
import java.util.Arrays;

public class BaseCacheHandler extends CacheHandler {

    @Override
    public Object load(MethodPoint point, String types, Status methodStatus) throws Exception {
        boolean enable = this.listenerProperties.isEnable();
        Method daoMethod = point.getMethod();
        String methodName = daoMethod.getName() ;
        //该PO领域的初始化
        //需要使用types表示其所涉及的缓存Bean的Class
        String key = this.encoder.getDaoKey(point, methodName, types, methodStatus);
        //对types进行处理,获取其主缓存Bean的Class名传入
        String mainType = types.split(Kache.SPLIT_TAG, 2)[0];
        //key拼接命名空间前缀
        Object[] daoArgs = point.getArgs();
        //以PO类型进行不同持久类领域的划分并拼接参数与方法作为幂等凭据
        String lockKey = (mainType + methodName + Arrays.hashCode(daoArgs));
        //获取缓存
        Object result = this.baseCacheManager.daoRead(key , mainType);
        if (result == null) {
            //为了防止缓存击穿，所以并不使用异步增加缓存，而采用同步锁限制
            //使用本地锁尽可能的减少纵向（单一节点）穿透，而允许横向（分布式）穿透
            //通过intern保证字符串都是源自于常量池而使得相同字符串为同一对象，保证锁的一致性
            synchronized (lockKey.intern()) {
                result = this.baseCacheManager.daoRead(key , mainType);
                if (result == null) {
                    //此处为真正未命中处，若置于上层则可能导致缓存穿透的线程一起被计数而导致不够准确
                    ListenerHandler.notHit(key, methodName, daoArgs, mainType, enable);
                    result = this.baseCacheManager.daoWrite(key , point, mainType);
                } else {
                    //将同步后获取缓存的线程的命中也计数
                    ListenerHandler.hit(key, methodName, daoArgs, mainType, enable);
                }
            }
        } else {
            ListenerHandler.hit(key, methodName, daoArgs, mainType, enable);
        }
        //空值替换
        if (result instanceof NullValue) {
            result = null ;
        }
        return result;
    }
}
