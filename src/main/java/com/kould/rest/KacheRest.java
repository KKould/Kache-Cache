package com.kould.rest;

import com.kould.listener.ListenerHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kache")
public class KacheRest {
    @GetMapping("/details")
    public Object details() {
        return ListenerHandler.details();
    }
}
