package com.kould.properties;

public class KeyProperties {

    public KeyProperties() {
    }

    public KeyProperties(String selectKey, String insertKey, String deleteKey, String updateKey, String selectByIdKey
            , String insertByIdKey, String updateByIdKey, String deleteByIdKey, boolean enable) {
        this.selectKey = selectKey;
        this.insertKey = insertKey;
        this.deleteKey = deleteKey;
        this.updateKey = updateKey;
        this.selectByIdKey = selectByIdKey;
        this.insertByIdKey = insertByIdKey;
        this.updateByIdKey = updateByIdKey;
        this.deleteByIdKey = deleteByIdKey;
        this.enable = enable;
    }

    private String selectKey = "select";

    private String insertKey = "insert";

    private String deleteKey = "delete";

    private String updateKey = "update";

    private String selectByIdKey = "selectById";

    private String insertByIdKey = "insertById";

    private String updateByIdKey = "updateById";

    private String deleteByIdKey = "deleteById";

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

    public String getInsertByIdKey() {
        return insertByIdKey;
    }

    public void setInsertByIdKey(String insertByIdKey) {
        this.insertByIdKey = insertByIdKey;
    }

    public String getUpdateByIdKey() {
        return updateByIdKey;
    }

    public void setUpdateByIdKey(String updateByIdKey) {
        this.updateByIdKey = updateByIdKey;
    }

    public String getDeleteByIdKey() {
        return deleteByIdKey;
    }

    public void setDeleteByIdKey(String deleteByIdKey) {
        this.deleteByIdKey = deleteByIdKey;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
