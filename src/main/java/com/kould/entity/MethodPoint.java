package com.kould.entity;

import java.lang.reflect.Method;

public class MethodPoint {

    public MethodPoint(Object target, Object[] args, Method method) {
        this.target = target;
        this.args = args;
        this.method = method;
    }

    private final Object target;

    private final Object[] args;

    private final Method method;

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object getTarget() {
        return target;
    }

    public Object execute() throws Exception {
        return method.invoke(target, args);
    }
}
