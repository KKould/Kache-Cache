package com.kould.entity;

import com.kould.properties.KeyProperties;

public class KeyEntity {

    private final String selectKey;

    private final String insertKey;

    private final String deleteKey;

    private final String updateKey;

    private final String selectStatusByIdKey;

    private final String insertStatusByIdKey;

    private final String updateStatusByIdKey;

    private final String deleteStatusByIdKey;

    private final boolean enable;

    public KeyEntity(KeyProperties keyProperties) {
        this.selectKey = keyProperties.getSelectKey();
        this.insertKey = keyProperties.getInsertKey();
        this.deleteKey = keyProperties.getDeleteKey();
        this.updateKey = keyProperties.getUpdateKey();
        this.enable = keyProperties.isEnable();
        this.selectStatusByIdKey = keyProperties.getSelectByIdKey();
        this.insertStatusByIdKey = keyProperties.getInsertByIdKey();
        this.updateStatusByIdKey = keyProperties.getUpdateByIdKey();
        this.deleteStatusByIdKey = keyProperties.getDeleteByIdKey();
    }

    public boolean selectKeyMatch(String methodName) {
        return enable && methodName.contains(selectKey);
    }

    public boolean insertKeyMatch(String methodName) {
        return enable && methodName.contains(insertKey);
    }

    public boolean deleteKeyMatch(String methodName) {
        return enable && methodName.contains(deleteKey);
    }

    public boolean updateKeyMatch(String methodName) {
        return enable && methodName.contains(updateKey);
    }

    public boolean selectByIdKeyEquals(String methodName) {
        return enable && selectStatusByIdKey.equals(methodName);
    }

    public boolean insertByIdKeyEquals(String methodName) {
        return enable && insertStatusByIdKey.equals(methodName);
    }

    public boolean updateByIdKeyEquals(String methodName) {
        return enable && updateStatusByIdKey.equals(methodName);
    }

    public boolean deleteByIdKeyEquals(String methodName) {
        return enable && deleteStatusByIdKey.equals(methodName);
    }

    public boolean isEnable() {
        return enable;
    }
}
