package com.kould.entity;

import com.kould.api.KacheEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

/**
 * Page类型的属性方法句柄封装
 * @param <T>
 */
public class PageDetails<T> {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Class<T> clazz;

    private final String recordName;

    private final Class<?> recordClass;

    private final MethodHandle getterForRecord;

    public PageDetails(Class<T> clazz, String recordName, Class<?> recordClass) throws NoSuchFieldException, IllegalAccessException {
        this.clazz = clazz;
        this.recordName = recordName;
        this.recordClass = recordClass;
        getterForRecord = init(clazz, recordName, recordClass);
    }

    private MethodHandle init(Class<T> clazz, String fieldName, Class<?> collectionClazz) throws NoSuchFieldException, IllegalAccessException {
        MethodHandles.Lookup privateLookupIn = MethodHandles.privateLookupIn(clazz, LOOKUP);
        return privateLookupIn.findGetter(clazz, fieldName, collectionClazz);
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getRecordName() {
        return recordName;
    }

    public Class<?> getRecordClass() {
        return recordClass;
    }

    public Collection<KacheEntity> getRecord(Object source) throws Throwable {
        return (Collection<KacheEntity>) getterForRecord.invoke(source);
    }
}
