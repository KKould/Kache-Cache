package com.kould.utils;

public class CloneUtils {

    private CloneUtils () {}

    public static <T> T cloneBean(T source) {
        return (T) KryoUtil.readFromByteArray(KryoUtil.writeToByteArray(source));
    }
}
