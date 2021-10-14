package com.kould.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.Method;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private Method method ;
    //Method无法被序列化
    private String methodName ;
    private Object arg ;
    private Class<?> clazz ;
    private Class<?> cacheClazz ;
}
