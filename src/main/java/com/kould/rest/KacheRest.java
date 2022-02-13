package com.kould.rest;

import com.kould.listener.ListenerHandler;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/kache")
public class KacheRest {
    @GetMapping("/details")
    @ResponseBody
    public Object details() {
        return ListenerHandler.details();
    }
}
