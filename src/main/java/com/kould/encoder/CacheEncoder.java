package com.kould.encoder;

public abstract class CacheEncoder {

    //ServiceMethodName、DaoMethodName、返回结果类型、Object[] PO参数Object[]
    public abstract String encode(String serviceMethodStatus, String serviceMethod, String daoEnityName, String daoMethodName, String daoArgs) ;
    public abstract String argsEncode(Object... args) ;
    public abstract String getPattern(String poName) ;
}
