package com.kould.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kache.listener")
public class ListenerProperties {
    private boolean enable = true;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
