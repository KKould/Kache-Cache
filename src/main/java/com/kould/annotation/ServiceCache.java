package com.kould.annotation;

import com.kould.config.Status;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceCache {

    //默认使用全删策略
    Status status() default Status.ALL;
}
