package com.kould.bean;

import lombok.*;

import java.io.Serializable;
import java.lang.reflect.Method;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private Method method ;
    private Object arg ;
    private Class<?> clazz ;
}