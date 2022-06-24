package com.kould.entity;

import com.kould.api.Kache;
import net.sf.cglib.beans.BeanCopier;
import org.objenesis.instantiator.ObjectInstantiator;

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

    private ObjectInstantiator<T> pageInstantiator;

    private MethodHandle getterForField;

    private BeanCopier beanCopier;

    public PageDetails(Class<T> clazz, String fieldName, Class<?> fieldClass) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.fieldClass = fieldClass;
        isFull = false;
        init(clazz);
    }

    public PageDetails(Class<T> clazz, String fieldName, Class<?> fieldClass, MethodHandle getterForField) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.fieldClass = fieldClass;
        this.getterForField = getterForField;
        isFull = true;
        init(clazz);
    }

    private void init(Class<T> clazz) {
        pageInstantiator = Kache.OBJENESIS.getInstantiatorOf(clazz);
        beanCopier = BeanCopier.create(clazz, clazz, false);
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

    public MethodHandle getGetterForField() {
        return getterForField;
    }

    public boolean isFull() {
        return isFull;
    }

    public T clone(T source) {
        T clone = pageInstantiator.newInstance();
        beanCopier.copy(source, clone, null);
        return clone;
    }
}
