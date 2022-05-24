package com.kould.function;

@FunctionalInterface
public interface ReadFunction {
    /**
     * 缓存读取函数式接口
     * @param key 缓存对应的键值
     * @param type 缓存Bean领域
     * @return 对应键缓存的值
     * @throws Exception 读取时潜在的错误
     */
    Object read(String key, String type) throws Exception;
}
