package com.kould.message;

import java.io.Serializable;
import java.lang.reflect.Method;

public class KacheMessage implements Serializable {

    private static final long serialVersionUID = -567614646101307581L;

    //Method无法被序列化
    private String methodName ;
    private Object arg ;
    private Class<?> clazz ;
    private Class<?> cacheClazz ;

    public static class Builder implements com.kould.type.Builder<KacheMessage> {
        private String methodName ;
        private Object arg ;
        private Class<?> clazz ;
        private Class<?> cacheClazz ;

        public Builder() { }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this ;
        }

        public Builder arg(Object arg) {
            this.arg = arg;
            return this ;
        }

        public Builder clazz(Class<?> clazz) {
            this.clazz = clazz;
            return this ;
        }

        public Builder cacheClazz(Class<?> cacheClazz) {
            this.cacheClazz = cacheClazz;
            return this ;
        }

        @Override
        public KacheMessage build() {
            return new KacheMessage(this) ;
        }
    }

    public KacheMessage() {
    }

    public KacheMessage(Method method, String methodName, Object arg, Class<?> clazz, Class<?> cacheClazz) {
        this.methodName = methodName;
        this.arg = arg;
        this.clazz = clazz;
        this.cacheClazz = cacheClazz;
    }

    public KacheMessage(Builder builder) {
        this.methodName = builder.methodName;
        this.arg = builder.arg;
        this.clazz = builder.clazz;
        this.cacheClazz = builder.cacheClazz;
    }

    public static Builder builder() {
        return new Builder() ;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object getArg() {
        return arg;
    }

    public void setArg(Object arg) {
        this.arg = arg;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getCacheClazz() {
        return cacheClazz;
    }

    public void setCacheClazz(Class<?> cacheClazz) {
        this.cacheClazz = cacheClazz;
    }
}
