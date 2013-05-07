package com.thoughtworks.orm.core;

import com.google.common.base.Function;
import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.Table;

import java.lang.reflect.Field;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;
import static com.thoughtworks.orm.util.Lang.getAnnotatedField;
import static com.thoughtworks.orm.util.Lang.makeThrow;
import static com.thoughtworks.orm.util.Lang.stackTrace;

class SQLGenerator {
    public static final String INSERTION_TEMPLATE = "INSERT INTO %s (%s) values(%s)";
    public static final String COLUMN_DELIMITER = ",";

    private final String table;

    private Class<?> entityClass;

    SQLGenerator(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.table = entityClass.getAnnotation(Table.class).value();
    }

    public String insertionSql(Object t) throws IllegalAccessException {
        String s = String.format(INSERTION_TEMPLATE, table, join(getFieldNames(), COLUMN_DELIMITER),
                join(getFieldValues(t), COLUMN_DELIMITER));
        System.out.println(s);
        return s;
    }

    private String join(Collection<String> items, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            builder.append(item).append(delimiter);
        }
        return builder.substring(0, builder.length() - delimiter.length());
    }

    private Collection<String> getFieldValues(final Object t) {
        return transform(getAnnotatedField(this.entityClass, Column.class), new Function<Field, String>() {
            @Override
            public String apply(java.lang.reflect.Field field) {
                try {
                    field.setAccessible(true);
                    return "'" + field.get(t) + "'";
                } catch (IllegalAccessException e) {
                    throw makeThrow("Error on get field values: ", stackTrace(e));
                }
            }
        });
    }

    private Collection<String> getFieldNames() {
        return transform(getAnnotatedField(this.entityClass, Column.class), new Function<Field, String>() {
            @Override
            public String apply(java.lang.reflect.Field field) {
                return field.getName();
            }
        });
    }
}