package com.kould.encoder;

import com.kould.config.Status;
import com.kould.proxy.MethodPoint;

import java.lang.reflect.Method;

public abstract class CacheEncoder {

    //ServiceMethodName、DaoMethodName、返回结果类型、Object[] PO参数Object[]
    public abstract String encode(String MethodStatus, String daoEnityName, String daoMethodName, String daoArgs) ;
    public abstract String argsEncode(Object... args) ;
    public abstract String getPattern(String poName) ;
    public abstract String getDaoKey(MethodPoint point, String methodName, Method method , Object args, String types, Status methodStatus) ;
}
