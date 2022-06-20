package com.kould.annotation;

import com.kould.entity.Status;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DaoSelect {
    //默认使用索引全删策略
    Status status() default Status.BY_FIELD;
    Class<?>[] involve() default {};
}
