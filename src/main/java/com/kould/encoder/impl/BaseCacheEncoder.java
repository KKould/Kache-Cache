package com.kould.encoder.impl;

import com.kould.config.KacheAutoConfig;
import com.kould.encoder.CacheEncoder;
import com.kould.utils.KryoUtil;

public class BaseCacheEncoder extends CacheEncoder {

    private static final BaseCacheEncoder INSTANCE = new BaseCacheEncoder() ;

    private BaseCacheEncoder() {}

    public static BaseCacheEncoder getInstance() {
        return INSTANCE ;
    }

    private static final long P = 31L;

    private static final long HASH = 0L ;


    @Override
    public String encode(String MethodStatus, String daoEnityName, String daoMethodName, String daoArgs) {
        return KacheAutoConfig.NO_ID_TAG +
                MethodStatus +
                daoEnityName +
                daoMethodName +
                daoArgs ;

    }

    @Override
    public String argsEncode(Object... args) {
        //此处可以返回空但是空会导致同一Service方法内调用同一Dao方法且Dao方法的参数不一致时会导致缓存误差
        //需要寻找循环依赖序列化方案-》Mybatis-Plus的Wrapper
        //new:使用Kryo序列化
        return String.valueOf(getHash(KryoUtil.writeToByteArray(args)));
    }

    @Override
    public String getPattern(String poName) {
        return "*" + poName + "*" ;
    }

    private Object readResolve() {
        return INSTANCE;
    }

    private static long getHash(byte[] bytes){
        long hash = HASH;
        for (byte datum : bytes) {
            hash = P * hash + datum ;
        }
        return hash + bytes.length;
    }
}
