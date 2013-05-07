package com.thoughtworks.orm.util;


import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Lang {

    public static RuntimeException makeThrow(String format, Object... args) {
        return new RuntimeException(String.format(format, args));
    }

    public static String stackTrace(Throwable e) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            buffer.append(element.toString()).append("\n");
        }
        return buffer.toString();
    }

    public static <T> T instanceFor(Class<?> clazz) {
        T instance;
        try {
            instance = (T) clazz.newInstance();
        } catch (Exception e) {
            throw makeThrow("Create instance failed for class %s, error: %s", clazz.getCanonicalName(), stackTrace(e));
        }
        return instance;
    }

    public static Collection<Field> getAnnotatedField(Class<?> clazz, final Class<? extends Annotation> annotationClazz) {

        Collection<Field> allFields = Arrays.asList(clazz.getDeclaredFields());

        return Collections2.filter(allFields, new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                return field.isAnnotationPresent(annotationClazz);
            }
        });
    }

    public static void info(String s) {
        System.out.println(s);
    }

    public static Long getId(Object obj) {
        Long id;
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            id = (Long) idField.get(obj);
        } catch (Exception e) {
            throw makeThrow("Exception encountered when get id of obj, : %s", stackTrace(e));
        }
        return id;
    }
}
