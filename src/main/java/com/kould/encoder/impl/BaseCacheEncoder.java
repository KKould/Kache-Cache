package com.kould.encoder.impl;

import com.kould.config.KacheAutoConfig;
import com.kould.encoder.CacheEncoder;
import com.kould.utils.KryoUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BaseCacheEncoder extends CacheEncoder {

    private static final char[] BYTE_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final BaseCacheEncoder INSTANCE = new BaseCacheEncoder() ;

    private BaseCacheEncoder() {}

    public static BaseCacheEncoder getInstance() {
        return INSTANCE ;
    }

    @Override
    public String encode(String serviceMethodStatus, String serviceMethod, String daoEnityName, String daoMethodName, String daoArgs) {
        return KacheAutoConfig.NO_ID_TAG +
                daoMethodName +
                daoArgs +
                serviceMethodStatus +
                serviceMethod +
                daoEnityName ;

    }

    @Override
    public String argsEncode(Object... args) {
        //此处可以返回空但是空会导致同一Service方法内调用同一Dao方法且Dao方法的参数不一致时会导致缓存误差
        //需要寻找循环依赖序列化方案-》Mybatis-Plus的Wrapper
        //new:使用Kryo序列化
        byte[] shas = getDigest("SHA").digest(KryoUtil.writeToByteArray(args));
        return encodeByte(shas).toString();
    }

    @Override
    public String getPattern(String poName) {
        return "*" + poName + "*" ;
    }

    private Object readResolve() {
        return INSTANCE;
    }

    private static String encodeByte(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder(20) ;
        for (byte datum : data) {
            //二进制编码转换
            stringBuilder.append(BYTE_CHARS[15 & datum]);
        }
        return stringBuilder.toString();
    }

    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException var2) {
            throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", var2);
        }
    }
}
