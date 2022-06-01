package com.kould.properties;

public class DataFieldProperties {

    public DataFieldProperties() {
    }

    public DataFieldProperties(String primaryKeyName, String recordsName) {
        this.primaryKeyName = primaryKeyName;
        this.recordsName = recordsName;
    }

    private String primaryKeyName = "id";

    private String recordsName = "records";

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public String getRecordsName() {
        return recordsName;
    }

    public void setRecordsName(String recordsName) {
        this.recordsName = recordsName;
    }
}
