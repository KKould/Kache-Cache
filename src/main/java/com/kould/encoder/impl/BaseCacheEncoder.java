package com.kould.encoder.impl;

import com.kould.api.Kache;
import com.kould.entity.Status;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.MethodPoint;
import com.kould.utils.KryoUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class BaseCacheEncoder extends CacheEncoder {

    private static final BaseCacheEncoder INSTANCE = new BaseCacheEncoder() ;

    private BaseCacheEncoder() {}

    public static BaseCacheEncoder getInstance() {
        return INSTANCE ;
    }

    private static final long P = 31L;

    private static final long HASH = 0L ;

    private static final String VERSION_SUFFIX = "VER.";

    private String keyJoint(String daoEntityName, String methodName, String args) {
        return Kache.CACHE_PREFIX +
                Kache.INDEX_TAG +
                daoEntityName +
                methodName +
                args;
    }

    private static final Map<String, LongAdder> VERSION_MAP = new ConcurrentHashMap<>();

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
//            return modifiedVersionSuffix(types, setKey2Id(idArg.toString(),types))[0];
        }else {
            String argsCode = argsEncode(point.getArgs());
            //使Key为各个参数编码后的一个特殊值
            return keyJoint(types, methodName, argsCode);
//            return modifiedVersionSuffix(types,keyJoint(types, methodName, argsCode))[0] ;
        }
    }

    @Override
    public String[] getId2Key(String type,String... ids) {
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Kache.CACHE_PREFIX + type + ids[i];
        }
        return ids;
//        return modifiedVersionSuffix(type, ids);
    }

    @Override
    public void versionUp(String type) {
        VERSION_MAP.computeIfAbsent(type, (k) -> new LongAdder()).increment();
    }

    private String setKey2Id(String id, String type) {
        return Kache.CACHE_PREFIX + type + id;
    }

    private Object readResolve() {
        return INSTANCE;
    }

    //用于缩略参数的大小并保持分布式环境下同等内容参数输出一致的哈希值
    private static long getHash(byte[] bytes){
        long hash = HASH;
        for (byte datum : bytes) {
            hash = P * hash + datum ;
        }
        return hash + bytes.length;
    }

//    private String[] modifiedVersionSuffix(String type, String... keys) {
//        long versionNum = VERSION_MAP.get(type).longValue();
//        for (int i = 0; i < keys.length; i++) {
//            keys[i] = keys[i] + VERSION_SUFFIX + versionNum;
//        }
//        return keys;
//    }
}
