package com.kould.core.impl;

import com.kould.config.Kache;
import com.kould.config.Status;
import com.kould.core.CacheHandler;
import com.kould.enity.NullValue;
import com.kould.function.KeyFunction;
import com.kould.function.ReadFunction;
import com.kould.function.WriteFunction;
import com.kould.listener.ListenerHandler;
import com.kould.proxy.MethodPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;

public class BaseCacheHandler extends CacheHandler {

    private static final Logger log = LoggerFactory.getLogger(BaseCacheHandler.class) ;

    @Override
    public Object load(MethodPoint point, boolean listenerEnable, ReadFunction readFunction
            , WriteFunction writeFunction, KeyFunction keyFunction , String types, Status methodStatus) throws Throwable {
        Object result;
        Method daoMethod = point.getMethod();
        String methodName = daoMethod.getName() ;
        //key拼接命名空间前缀
        Object[] daoArgs = point.getArgs();
        //以PO类型进行不同持久类领域的划分并拼接参数与方法作为锁对象，而intern方法是必要的
        String lockKey = (types + methodName + Arrays.hashCode(daoArgs));
        //该PO领域的初始化
        try {
            String key = Kache.CACHE_PREFIX + keyFunction.encode(point, methodName, daoMethod, daoArgs, types, methodStatus);
            //获取缓存
            result = readFunction.read(key , types);
            if (result == null) {
                //为了防止缓存击穿，所以并不使用异步增加缓存，而采用同步锁限制
                //使用本地锁尽可能的减少纵向（单一节点）穿透，而允许横向（分布式）穿透
                //通过intern保证字符串都是源自于常量池而使得相同字符串为同一对象，保证锁的一致性
                synchronized (lockKey.intern()) {
                    result = readFunction.read(key , types);
                    if (result == null) {
                        //此处为真正未命中处，若置于上层则可能导致缓存穿透的线程一起被计数而导致不够准确
                        ListenerHandler.notHit(key, methodName, daoArgs, types, listenerEnable);
                        result = writeFunction.write(key , point, types);
                    } else {
                        //将同步后获取缓存的线程的命中也计数
                        ListenerHandler.hit(key, methodName, daoArgs, types, listenerEnable);
                    }
                }
            } else {
                ListenerHandler.hit(key, methodName, daoArgs, types, listenerEnable);
            }
            //空值替换
            if (result instanceof NullValue) {
                result = null ;
            }
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            throw e ;
        }
        return result;
    }
}
