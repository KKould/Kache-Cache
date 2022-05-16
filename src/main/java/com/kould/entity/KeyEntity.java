package com.kould.entity;

import com.kould.properties.KeyProperties;

public class KeyEntity {

    private final String selectKey;

    private final String insertKey;

    private final String deleteKey;

    private final String updateKey;

    private final String selectStatusByIdKey;

    private final boolean enable;

    public KeyEntity(KeyProperties keyProperties) {
        this.selectKey = keyProperties.getSelectKey();
        this.insertKey = keyProperties.getInsertKey();
        this.deleteKey = keyProperties.getDeleteKey();
        this.updateKey = keyProperties.getUpdateKey();
        this.selectStatusByIdKey = keyProperties.getSelectByIdKey();
        this.enable = keyProperties.isEnable();
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
}
