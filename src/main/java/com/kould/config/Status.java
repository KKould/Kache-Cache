package com.kould.config;

public enum Status {
    LIKE(KacheAutoConfig.SERVICE_LIKE), IS(KacheAutoConfig.SERVICE_IS), ALL(KacheAutoConfig.SERVICE_ALL), NO_ARG(KacheAutoConfig.SERVICE_NO_ARG), BY_ID(KacheAutoConfig.SERVICE_BY_ID);


    private final String value ;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
