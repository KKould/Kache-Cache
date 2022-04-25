package com.kould.config;

public class ListenerProperties {

    public ListenerProperties() {
    }

    public ListenerProperties(boolean enable) {
        this.enable = enable;
    }

    private boolean enable = true;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
