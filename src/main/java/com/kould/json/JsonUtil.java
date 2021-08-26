package com.kould.json;

import java.lang.reflect.Type;

public interface JsonUtil {
    String obj2Str(Object obj) ;
    <T> T str2Obj(String str, Type type) ;
}
