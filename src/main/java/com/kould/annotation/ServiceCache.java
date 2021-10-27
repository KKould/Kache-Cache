package com.kould.annotation;

import com.kould.config.KacheAutoConfig;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceCache {

    //默认使用全删策略
    KacheAutoConfig.Status status() default KacheAutoConfig.Status.ALL;
}
