package com.kould.encoder.impl;

import com.google.gson.reflect.TypeToken;
import com.kould.config.KacheAutoConfig;
import com.kould.encoder.CacheEncoder;
import com.kould.utils.KryoUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BaseCacheEncoder extends CacheEncoder {

    private static final BaseCacheEncoder INSTANCE = new BaseCacheEncoder() ;

    private BaseCacheEncoder() {}

    public static BaseCacheEncoder getInstance() {
        return INSTANCE ;
    }

    @Override
    public String encode(Object dto,String serviceMethodStatus, String serviceMethod, String daoEnityName, String daoMethodName, String daoArgs) {
        return KacheAutoConfig.NO_ID_TAG +
                daoMethodName +
                daoArgs +
                serviceMethodStatus +
                serviceMethod +
                daoEnityName +
                jsonUtil.obj2Str(dto) ;

    }

    @Override
    public String argsEncode(Object... args) {
        //此处可以返回空但是空会导致同一Service方法内调用同一Dao方法且Dao方法的参数不一致时会导致缓存误差
        //需要寻找循环依赖序列化方案-》Mybaits-Plus的Wrapper
        //new:使用Kryo序列化
        return KryoUtil.writeToString(args) ;
    }

    @Override
    public <T> T decode(String key, Type type, String poName) {
        StringBuilder sb = new StringBuilder(key) ;
        int i = sb.indexOf(poName + "{");
        if ( i > 0) {
            sb.delete(0,i) ;
            int j = sb.indexOf("{");
            if (j > 0){
                sb.delete(0,j) ;
                return jsonUtil.str2Obj(sb.toString(), type) ;
            }
        }
        return null;
    }

    @Override
    public Map<String, String> section2Field(Object key, String method) {
        return jsonUtil.str2Obj(jsonUtil.obj2Str(key), new TypeToken<HashMap<String, String>>() {}.getType());
    }

    @Override
    public String getPattern(String poName) {
        return "*" + poName + "*" ;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
