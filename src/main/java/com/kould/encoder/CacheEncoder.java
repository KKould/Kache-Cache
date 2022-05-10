package com.kould.encoder;

import com.kould.entity.Status;
import com.kould.proxy.MethodPoint;

public abstract class CacheEncoder {

    public abstract String argsEncode(Object... args);
    public abstract String getPattern(String poName);
    public abstract String getDaoKey(MethodPoint point, String methodName, String types, Status methodStatus) ;
    public abstract String getId2Key(String id, String type);
}
