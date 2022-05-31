package com.kould.entity;

import java.io.Serializable;

/**
 * 用于封装传递缓存的摘要信息
 */
public class KacheMessage implements Serializable {

    private static final long serialVersionUID = -567614646101307581L;

    // 用于消费幂等标识
    private final String id;

    private final String methodName ;
    private final Object[] arg ;
    private final Class<?> cacheClazz ;
    private final String type;

    public static class Builder implements com.kould.type.Builder<KacheMessage> {
        private String id;
        private String methodName ;
        private Object[] arg ;
        private Class<?> cacheClazz ;
        private String type;

        public Builder() { }

        public Builder id(String id) {
            this.id = id;
            return this ;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this ;
        }

        public Builder arg(Object[] arg) {
            this.arg = arg;
            return this ;
        }

        public Builder cacheClazz(Class<?> cacheClazz) {
            this.cacheClazz = cacheClazz;
            return this ;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        @Override
        public KacheMessage build() {
            return new KacheMessage(this) ;
        }
    }

    public KacheMessage(String id, String methodName, Object[] arg, Class<?> cacheClazz, String type) {
        this.id = id;
        this.methodName = methodName;
        this.arg = arg;
        this.cacheClazz = cacheClazz;
        this.type = type;
    }

    public KacheMessage(Builder builder) {
        this.id = builder.id;
        this.methodName = builder.methodName;
        this.arg = builder.arg;
        this.cacheClazz = builder.cacheClazz;
        this.type = builder.type;
    }

    public static Builder builder() {
        return new Builder() ;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArg() {
        return arg;
    }

    public Class<?> getCacheClazz() {
        return cacheClazz;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
