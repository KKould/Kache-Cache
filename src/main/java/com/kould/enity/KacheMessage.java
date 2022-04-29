package com.kould.enity;

import java.io.Serializable;

//不允许修改值
public class KacheMessage implements Serializable {

    private static final long serialVersionUID = -567614646101307581L;

    private String methodName ;
    private Object[] arg ;
    private Class<?> cacheClazz ;
    private String types;

    public static class Builder implements com.kould.type.Builder<KacheMessage> {
        private String methodName ;
        private Object[] arg ;
        private Class<?> cacheClazz ;
        private String types;

        public Builder() { }

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

        public Builder types(String types) {
            this.types = types;
            return this;
        }

        @Override
        public KacheMessage build() {
            return new KacheMessage(this) ;
        }
    }

    public KacheMessage() {
    }

    public KacheMessage(String methodName, Object[] arg, Class<?> cacheClazz, String types) {
        this.methodName = methodName;
        this.arg = arg;
        this.cacheClazz = cacheClazz;
        this.types = types;
    }

    public KacheMessage(Builder builder) {
        this.methodName = builder.methodName;
        this.arg = builder.arg;
        this.cacheClazz = builder.cacheClazz;
        this.types = builder.types;
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

    public String getTypes() {
        return types;
    }
}
