package com.kould.bean;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private String method ;
    private Object arg ;
    private Class<?> clazz ;
}