package com.kould.entity;

public class RegexEntity {

    private final String selectRegex;

    private final String insertRegex;

    private final String deleteRegex;

    private final String updateRegex;

    private final String selectStatusByIdRegex;

    public RegexEntity(String selectRegex, String insertRegex, String deleteRegex, String updateRegex, String selectStatusByIdRegex) {
        this.selectRegex = selectRegex;
        this.insertRegex = insertRegex;
        this.deleteRegex = deleteRegex;
        this.updateRegex = updateRegex;
        this.selectStatusByIdRegex = selectStatusByIdRegex;
    }

    public String getSelectRegex() {
        return selectRegex;
    }

    public String getInsertRegex() {
        return insertRegex;
    }

    public String getDeleteRegex() {
        return deleteRegex;
    }

    public String getUpdateRegex() {
        return updateRegex;
    }

    public String getSelectStatusByIdRegex() {
        return selectStatusByIdRegex;
    }
}
