package com.kould.entity;

import com.kould.api.Kache;
import net.sf.cglib.beans.BeanCopier;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Page类型的属性方法句柄封装
 * @param <T>
 */
public class PageDetails<T> {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Class<T> clazz;

    private final String fieldName;

    private final Class<?> fieldClass;

    private final MethodHandle getterForField;

    private ObjectInstantiator<T> pageInstantiator;

    private BeanCopier beanCopier;

    public PageDetails(Class<T> clazz, String fieldName, Class<?> fieldClass) throws NoSuchFieldException, IllegalAccessException {
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.fieldClass = fieldClass;
        getterForField = init(clazz, fieldName, fieldClass);
    }

    private MethodHandle init(Class<T> clazz, String fieldName, Class<?> collectionClazz) throws NoSuchFieldException, IllegalAccessException {
        pageInstantiator = Kache.OBJENESIS.getInstantiatorOf(clazz);
        beanCopier = BeanCopier.create(clazz, clazz, false);
        MethodHandles.Lookup privateLookupIn = MethodHandles.privateLookupIn(clazz, LOOKUP);
        return privateLookupIn.findGetter(clazz, fieldName, collectionClazz);
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

    public T clone(T source) {
        T clone = pageInstantiator.newInstance();
        beanCopier.copy(source, clone, null);
        return clone;
    }
}
