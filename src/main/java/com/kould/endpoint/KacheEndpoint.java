package com.kould.endpoint;

import com.kould.listener.ListenerHandler;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Map;

@Endpoint(id = "kache")
public class KacheEndpoint {
    @ReadOperation //显示监控指标
    public Map<String,Object> info(){
        return ListenerHandler.details();
    }
}
