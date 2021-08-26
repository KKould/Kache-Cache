package com.kould.json;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
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
