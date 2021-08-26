package com.kould.annotation;

import com.kould.bean.KacheConfig;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceCache {

    KacheConfig.Status status() default KacheConfig.Status.IS;
}
