package com.kould.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kache.interprocess-cache")
public class InterprocessCacheProperties {
    private boolean enable = true;

    private int size = 50;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}