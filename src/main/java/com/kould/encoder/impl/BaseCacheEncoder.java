package com.kould.encoder.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.reflect.TypeToken;
import com.kould.KryoUtil;
import com.kould.annotation.ServiceCache;
import com.kould.encoder.CacheEncoder;
import com.kould.json.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BaseCacheEncoder implements CacheEncoder {

    @Autowired
    private JsonUtil jsonUtil ;

    @Override
    public String encode(Object dto, Method serviceMethod, String daoEnityName, String daoMethodName, String daoArgs) {
        return daoMethodName +
                daoArgs +
                serviceMethod.getAnnotation(ServiceCache.class).status().getValue() +
                serviceMethod.getName() +
                daoEnityName +
                jsonUtil.obj2Str(dto) ;

    }

    @Override
    public String argsEncode(Object... args) {
        //此处可以返回空但是空会导致同一Service方法内调用同一Dao方法且Dao方法的参数不一致时会导致缓存误差
        //需要寻找循环依赖序列化方案-》Mybaits-Plus的Wrapper
        //new:使用Kryo序列化
        StringBuilder result = new StringBuilder() ;
        try {
            for (Object arg : args) {
                result.append(KryoUtil.writeToString(arg)) ;
            }
        } catch (Exception e) {
            throw e ;
        }
        return result.toString() ;
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

    @Override
    public <T> Class<T> getPackageClass() {
        return (Class<T>) Page.class;
    }
}
