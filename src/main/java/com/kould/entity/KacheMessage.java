package com.kould.entity;

import com.kould.api.KacheEntity;

import java.io.Serializable;

/**
 * 用于封装传递缓存的摘要信息
 */
public class KacheMessage implements Serializable {

    private static final long serialVersionUID = -567614646101307581L;

    // 用于消费幂等标识
    private final String id;

    private final String methodName ;
    private final Object[] args;
    private final Class<? extends KacheEntity> cacheClazz ;
    private final String type;

    public static class Builder {
        private String id;
        private String methodName ;
        private Object[] args;
        private Class<? extends KacheEntity> cacheClazz ;
        private String type;

        public Builder id(String id) {
            this.id = id;
            return this ;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this ;
        }

        public Builder args(Object[] args) {
            this.args = args;
            return this ;
        }

        public Builder cacheClazz(Class<? extends KacheEntity> cacheClazz) {
            this.cacheClazz = cacheClazz;
            return this ;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public KacheMessage build() {
            return new KacheMessage(this) ;
        }
    }

    public KacheMessage(String id, String methodName, Object[] args, Class<? extends KacheEntity> cacheClazz, String type) {
        this.id = id;
        this.methodName = methodName;
        this.args = args;
        this.cacheClazz = cacheClazz;
        this.type = type;
    }

    public KacheMessage(Builder builder) {
        this.id = builder.id;
        this.methodName = builder.methodName;
        this.args = builder.args;
        this.cacheClazz = builder.cacheClazz;
        this.type = builder.type;
    }

    public static Builder builder() {
        return new Builder() ;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public Class<? extends KacheEntity> getCacheClazz() {
        return cacheClazz;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
