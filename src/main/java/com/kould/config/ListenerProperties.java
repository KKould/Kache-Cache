package com.kould.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kache.listener")
public class ListenerProperties {
    private boolean enable ;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
