package com.kould.properties;

public class KeyProperties {

    public KeyProperties() {
    }

    public KeyProperties(String selectKey, String insertKey, String deleteKey, String updateKey, String selectByIdKey, boolean enable) {
        this.selectKey = selectKey;
        this.insertKey = insertKey;
        this.deleteKey = deleteKey;
        this.updateKey = updateKey;
        this.selectByIdKey = selectByIdKey;
        this.enable = enable;
    }

    private String selectKey = "select";

    private String insertKey = "insert";

    private String deleteKey = "delete";

    private String updateKey = "update";

    private String selectByIdKey = "selectById";

    private boolean enable = true;

    public String getSelectKey() {
        return selectKey;
    }

    public void setSelectKey(String selectKey) {
        this.selectKey = selectKey;
    }

    public String getInsertKey() {
        return insertKey;
    }

    public void setInsertKey(String insertKey) {
        this.insertKey = insertKey;
    }

    public String getDeleteKey() {
        return deleteKey;
    }

    public void setDeleteKey(String deleteKey) {
        this.deleteKey = deleteKey;
    }

    public String getUpdateKey() {
        return updateKey;
    }

    public void setUpdateKey(String updateKey) {
        this.updateKey = updateKey;
    }

    public String getSelectByIdKey() {
        return selectByIdKey;
    }

    public void setSelectByIdKey(String selectByIdKey) {
        this.selectByIdKey = selectByIdKey;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
