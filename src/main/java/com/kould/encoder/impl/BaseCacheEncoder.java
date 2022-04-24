package com.kould.encoder.impl;

import com.kould.annotation.DaoSelect;
import com.kould.config.KacheAutoConfig;
import com.kould.config.Status;
import com.kould.encoder.CacheEncoder;
import com.kould.utils.KryoUtil;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

public class BaseCacheEncoder extends CacheEncoder {

    private static final BaseCacheEncoder INSTANCE = new BaseCacheEncoder() ;

    private BaseCacheEncoder() {}

    public static BaseCacheEncoder getInstance() {
        return INSTANCE ;
    }

    private static final long P = 31L;

    private static final long HASH = 0L ;


    @Override
    public String encode(String MethodStatus, String daoEnityName, String methodName, String args) {
        return KacheAutoConfig.NO_ID_TAG +
                MethodStatus +
                daoEnityName +
                methodName +
                args ;

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
    public String getDaoKey(ProceedingJoinPoint point, String methodName, Method method, Object args, String types) {
        //判断serviceMethod的是否为通过id获取数据
        //  若是则直接使用id进行获取
        //  若否则经过编码后进行获取
        //信息摘要收集
        //获取DAO方法签名
        if (methodName.equals(KacheAutoConfig.MYBATIS_PLUS_MAPPER_SELECT_BY_ID)) {
            return setKey2Id(point, types);
        }
        DaoSelect daoSelect = method.getAnnotation(DaoSelect.class);
        String methodStatus = null ;
        if (daoSelect != null) {
            methodStatus = daoSelect.status().getValue();
        } else {
            methodStatus = Status.BY_FIELD.getValue() ;
        }
        if (methodStatus.equals(KacheAutoConfig.SERVICE_BY_ID)) {
            //使Key为ID
            return setKey2Id(point,types);
        }else {
            String argsCode = argsEncode(args);
            //使Key为各个参数编码后的一个特殊值
            return encode(methodStatus, types, methodName, argsCode) ;
        }
    }

    private String setKey2Id(ProceedingJoinPoint point, String type) {
        //使Key为ID
        Object[] args = point.getArgs();
        return type + args[0].toString();
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
