package com.kould.entity;

import java.lang.invoke.MethodHandle;

/**
 * Page类型的属性方法句柄封装
 * 当方法句柄为Null时，在Kache组装时自动填充Setter、Getter的方法句柄
 * @param <T>
 */
public class PageDetails<T> {

    private final Class<T> clazz;

    private final String fieldName;

    private final Class<?> fieldClass;

    private final boolean isFull;

    private MethodHandle setterForField;

    private MethodHandle getterForField;

    public PageDetails(Class<T> clazz, String fieldName, Class<?> fieldClass) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.fieldClass = fieldClass;
        isFull = false;
    }

    public PageDetails(Class<T> clazz, String fieldName, Class<?> fieldClass, MethodHandle setterForField, MethodHandle getterForField) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.fieldClass = fieldClass;
        this.setterForField = setterForField;
        this.getterForField = getterForField;
        isFull = true;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }

    public MethodHandle getSetterForField() {
        return setterForField;
    }

    public MethodHandle getGetterForField() {
        return getterForField;
    }

    public boolean isFull() {
        return isFull;
    }
}
