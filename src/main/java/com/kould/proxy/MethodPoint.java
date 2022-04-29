package com.kould.proxy;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class MethodPoint {

    public MethodPoint(MethodProxy methodProxy, Object target, Object[] args, Method method) {
        this.methodProxy = methodProxy;
        this.target = target;
        this.args = args;
        this.method = method;
    }

    private final MethodProxy methodProxy;

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

    public Object execute() throws Throwable {
        return methodProxy.invoke(target, args);
    }
}
