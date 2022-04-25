package com.kould.config;

public class InterprocessCacheProperties {

    public InterprocessCacheProperties() {
    }

    public InterprocessCacheProperties(boolean enable, int size) {
        this.enable = enable;
        this.size = size;
    }

    private boolean enable = false;

    private int size =200;

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
