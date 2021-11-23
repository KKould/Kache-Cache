package com.kould.encoder;

import java.lang.reflect.Type;
import java.util.Map;

public interface CacheEncoder {
    // DTO参数、ServiceMethodName、DaoMethodName、返回结果类型、Object[] PO参数Object[]
    String encode(Object dto,String serviceMethodStatus, String serviceMethod, String daoEnityName, String daoMethodName, String daoArgs) ;
    String argsEncode(Object... args) ;
    <T> T decode(String key, Type type, String serviceMethodName) ;
    Map<String,String> section2Field(Object key, String method) ;
    String getPattern(String poName) ;
}
