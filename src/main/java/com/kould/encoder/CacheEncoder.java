package com.kould.encoder;

import com.kould.entity.Status;
import com.kould.entity.MethodPoint;

public abstract class CacheEncoder {

    public abstract String getPattern(String poName);
    public abstract String getDaoKey(MethodPoint point, String methodName, String types, Status methodStatus) ;
    public abstract String[] getId2Key(String type,String... ids);
    public abstract void versionUp(String type);
}
