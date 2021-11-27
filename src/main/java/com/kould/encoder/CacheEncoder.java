package com.kould.encoder;

import com.kould.json.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.Map;

public abstract class CacheEncoder {
    @Autowired
    protected JsonUtil jsonUtil ;

    // DTO参数、ServiceMethodName、DaoMethodName、返回结果类型、Object[] PO参数Object[]
    public abstract String encode(Object dto,String serviceMethodStatus, String serviceMethod, String daoEnityName, String daoMethodName, String daoArgs) ;
    public abstract String argsEncode(Object... args) ;
    public abstract <T> T decode(String key, Type type, String serviceMethodName) ;
    public abstract Map<String,String> section2Field(Object key, String method) ;
    public abstract String getPattern(String poName) ;
}
