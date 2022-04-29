package com.kould.filter;

import net.sf.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;

public class CRUDFilter implements CallbackFilter {
    @Override
    public int accept(Method method) {
        return 0;
    }
}
