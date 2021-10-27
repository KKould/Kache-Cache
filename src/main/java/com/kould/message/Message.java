package com.kould.message;

import java.io.Serializable;
import java.lang.reflect.Method;

public class Message implements Serializable {
    private Method method ;
    //Method无法被序列化
    private String methodName ;
    private Object arg ;
    private Class<?> clazz ;
    private Class<?> cacheClazz ;

    public static class Builder {
        private Method method ;
        private String methodName ;
        private Object arg ;
        private Class<?> clazz ;
        private Class<?> cacheClazz ;

        public Builder() { }

        public Builder method(Method method) {
            this.method = method;
            return this ;
        }

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

        public Message build() {
            return new Message(this) ;
        }
    }

    public Message() {
    }

    public Message(Method method, String methodName, Object arg, Class<?> clazz, Class<?> cacheClazz) {
        this.method = method;
        this.methodName = methodName;
        this.arg = arg;
        this.clazz = clazz;
        this.cacheClazz = cacheClazz;
    }

    public Message(Builder builder) {
        this.method = builder.method ;
        this.methodName = builder.methodName;
        this.arg = builder.arg;
        this.clazz = builder.clazz;
        this.cacheClazz = builder.cacheClazz;
    }

    public static Builder builder() {
        return new Builder() ;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
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
