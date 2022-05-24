package com.kould.function;

import com.kould.entity.Status;
import com.kould.entity.MethodPoint;

@FunctionalInterface
public interface KeyFunction {
    /**
     * 缓存Key编码函数式接口
     * @param point 方法代理切入点
     * @param methodName 方法名
     * @param type Bean领域
     * @param methodStatus 方法状态
     * @return 缓存特殊编码Key
     */
    String encode(MethodPoint point, String methodName, String type, Status methodStatus) ;
}
