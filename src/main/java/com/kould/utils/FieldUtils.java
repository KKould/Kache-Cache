package com.kould.utils;

import java.lang.reflect.Field;

public class FieldUtils {

    private FieldUtils() {
        throw new IllegalStateException("Utility Class!");
    }

    /**
     * 类反射获取属性方法并解除安全检查
     * @param clazz 目标Class
     * @param fieldName 属性名
     * @return Field 去除安全检查的属性
     * @throws NoSuchFieldException
     */
    public static Field getFieldByNameAndClass(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field field ;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            //此处用于解决继承导致的getDeclaredField不能直接获取父类属性的问题
            field = clazz.getSuperclass().getDeclaredField(fieldName);
        }
        field.setAccessible(true);
        return field;
    }
}
