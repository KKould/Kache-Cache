package com.kould.json;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class GsonUtil implements JsonUtil{

    private static Gson gson ;

    public GsonUtil() {
        gson = new Gson() ;
    }

    @Override
    public String obj2Str(Object obj) {
        return gson.toJson(obj);
    }

    @Override
    public <T> T str2Obj(String str, Type type) {
        return gson.fromJson(str, type);
    }
}
