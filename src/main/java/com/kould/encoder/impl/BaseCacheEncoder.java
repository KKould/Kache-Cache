package com.kould.encoder.impl;

import com.kould.api.Kache;
import com.kould.config.Status;
import com.kould.encoder.CacheEncoder;
import com.kould.proxy.MethodPoint;
import com.kould.utils.KryoUtil;

import java.io.Serializable;

public class BaseCacheEncoder extends CacheEncoder {

    private static final BaseCacheEncoder INSTANCE = new BaseCacheEncoder() ;

    private BaseCacheEncoder() {}

    public static BaseCacheEncoder getInstance() {
        return INSTANCE ;
    }

    private static final long P = 31L;

    private static final long HASH = 0L ;

    private String keyJoint(String daoEntityName, String methodName, String args) {
        return Kache.CACHE_PREFIX +
                Kache.NO_ID_TAG +
                daoEntityName +
                methodName +
                args;
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

    @Override
    public String getDaoKey(MethodPoint point, String methodName, String types, Status methodStatus) {
        //判断serviceMethod的是否为通过id获取数据
        //  若是则直接使用id进行获取
        //  若否则经过编码后进行获取
        //信息摘要收集
        //获取DAO方法签名
        if (methodStatus.equals(Status.BY_ID)) {
            //使Key为ID
            Object idArg = point.getArgs()[0];
            assert idArg instanceof Serializable;
            return setKey2Id(idArg.toString(),types);
        }else {
            String argsCode = argsEncode(point.getArgs());
            //使Key为各个参数编码后的一个特殊值
            return keyJoint(types, methodName, argsCode) ;
        }
    }

    @Override
    public String getId2Key(String id, String type) {
        return Kache.CACHE_PREFIX + type + id;
    }

    private String setKey2Id(String id, String type) {
        return Kache.CACHE_PREFIX + type + id;
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
