package com.kould.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldUtils {

    private FieldUtils() {
        throw new IllegalStateException("Utility Class!");
    }

    /**
     * 类反射获取属性方法并解除安全检查
     * @param clazz 目标Class
     * @param fieldName 属性名
     * @return Field 去除安全检查的属性
     * @throws NoSuchFieldException 不匹配Field异常
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

    /**
     * 获取目标class以及其class的父类属性一并返回
     * @param clazz 目标Bean的class
     * @return 该Bean所有的Field
     */
    public static List<Field> getAllField(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
