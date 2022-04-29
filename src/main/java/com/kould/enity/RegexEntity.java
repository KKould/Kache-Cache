package com.kould.enity;

public class RegexEntity {

    private final String selectRegex;

    private final String insertRegex;

    private final String deleteRegex;

    private final String updateRegex;

    public RegexEntity(String selectRegex, String insertRegex, String deleteRegex, String updateRegex) {
        this.selectRegex = selectRegex;
        this.insertRegex = insertRegex;
        this.deleteRegex = deleteRegex;
        this.updateRegex = updateRegex;
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
}
