package com.kould.test.entity;

public class TestEntity {

    public TestEntity() {
    }

    public TestEntity(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
