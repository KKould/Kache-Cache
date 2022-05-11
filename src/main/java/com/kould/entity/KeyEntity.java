package com.kould.entity;

public class KeyEntity {

    private final String selectKey;

    private final String insertKey;

    private final String deleteKey;

    private final String updateKey;

    private final String selectStatusByIdKey;

    public KeyEntity(String selectKey, String insertKey, String deleteKey
            , String updateKey, String selectStatusByIdKey) {
        this.selectKey = selectKey;
        this.insertKey = insertKey;
        this.deleteKey = deleteKey;
        this.updateKey = updateKey;
        this.selectStatusByIdKey = selectStatusByIdKey;
    }

    public boolean selectKeyMatch(String methodName) {
        return methodName.contains(selectKey);
    }

    public boolean insertKeyMatch(String methodName) {
        return methodName.contains(insertKey);
    }

    public boolean deleteKeyMatch(String methodName) {
        return methodName.contains(deleteKey);
    }

    public boolean updateKeyMatch(String methodName) {
        return methodName.contains(updateKey);
    }

    public boolean selectByIdKeyEquals(String methodName) {
        return selectStatusByIdKey.equals(methodName);
    }
}
