package com.kould.test.entity;

import com.kould.api.KacheEntity;

public class TestEntity implements KacheEntity {

    public TestEntity() {
    }

    public TestEntity(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    private Long id;

    private String message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getPrimaryKey() {
        return id.toString();
    }
}
