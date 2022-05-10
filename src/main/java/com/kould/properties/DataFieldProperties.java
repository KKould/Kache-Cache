package com.kould.properties;

public class DataFieldProperties {

    public DataFieldProperties() {
    }

    public DataFieldProperties(String primaryKeyName, String recordsName, String declareType) {
        this.primaryKeyName = primaryKeyName;
        this.recordsName = recordsName;
        this.declareType = declareType;
    }

    private String primaryKeyName = "id";

    private String recordsName = "records";

    private String declareType = "java.util.List";

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

    public String getDeclareType() {
        return declareType;
    }

    public void setDeclareType(String declareType) {
        this.declareType = declareType;
    }
}
