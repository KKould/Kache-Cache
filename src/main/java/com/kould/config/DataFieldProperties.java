package com.kould.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kache.data-field")
public class DataFieldProperties {
    private String name ;

    private String declareType ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeclareType() {
        return declareType;
    }

    public void setDeclareType(String declareType) {
        this.declareType = declareType;
    }
}
