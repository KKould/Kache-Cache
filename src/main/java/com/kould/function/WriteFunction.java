package com.kould.function;

import com.kould.entity.MethodPoint;

@FunctionalInterface
public interface WriteFunction {
    /**
     * 缓存写入函数式接口
     * @param key 缓存的键值
     * @param point 方法代理切入点
     * @param type 缓存Bean领域
     * @return 数据库中读取的结果
     * @throws Exception 处理时潜在的错误
     */
    Object write(String key, MethodPoint point, String type) throws Exception;
}
