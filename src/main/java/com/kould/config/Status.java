package com.kould.config;

public enum Status {
    BY_FIELD(KacheAutoConfig.SERVICE_BY_FIELD), BY_ID(KacheAutoConfig.SERVICE_BY_ID);


    private final String value ;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
