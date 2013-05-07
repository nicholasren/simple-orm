package com.thoughtworks.orm.core;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.Table;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;
import static com.thoughtworks.orm.util.Lang.*;

class StatementGenerator {
    public static final String INSERTION_TEMPLATE = "INSERT INTO %s (%s) values(%s)";
    public static final String COLUMN_DELIMITER = ",";

    private final String table;
    private Class<?> entityClass;
    private java.sql.Connection connection;

    private Function<Field, String> getNameFunction = new Function<Field, String>() {
        @Override
        public String apply(Field field) {
            return field.getName();
        }
    };

    StatementGenerator(Class<?> entityClass, Connection connection) {
        this.entityClass = entityClass;
        this.table = entityClass.getAnnotation(Table.class).value();
        this.connection = connection;
    }

    public PreparedStatement insertion(Object obj) {
        String sql = String.format(INSERTION_TEMPLATE, table, join(getFieldNames(), COLUMN_DELIMITER),
                join(getFieldValuePlaceHolders(obj), COLUMN_DELIMITER));

        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement(sql);

            Object[] fieldValues = getFieldValues(obj).toArray();

            for (int i = 0; i < fieldValues.length; i++) {
                preparedStatement.setObject(i + 1, fieldValues[i]);
            }

        } catch (SQLException e) {
            throw makeThrow("Exception encountered when generating insertion statement: %s", stackTrace(e));
        }
        return preparedStatement;
    }

    private String join(Collection<String> items, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            builder.append(item).append(delimiter);
        }
        return builder.substring(0, builder.length() - delimiter.length());
    }

    private Collection<String> getFieldValuePlaceHolders(final Object t) {
        return transform(getSortedAnnotatedField(), new Function<Field, String>() {
            @Override
            public String apply(java.lang.reflect.Field field) {
                return "?";
            }
        });
    }

    private Collection<String> getFieldNames() {
        return transform(getSortedAnnotatedField(), new Function<Field, String>() {
            @Override
            public String apply(java.lang.reflect.Field field) {
                return field.getName();
            }
        });
    }

    private Collection<Object> getFieldValues(final Object t) {
        return transform(getSortedAnnotatedField(), new Function<Field, Object>() {
            @Override
            public Object apply(java.lang.reflect.Field field) {
                try {
                    field.setAccessible(true);
                    return field.get(t);
                } catch (IllegalAccessException e) {
                    throw makeThrow("Got error when get field values %s", stackTrace(e));
                }
            }
        });
    }

    private Collection<Field> getSortedAnnotatedField() {
        return Ordering.natural().onResultOf(getNameFunction).sortedCopy(getAnnotatedField(this.entityClass, Column.class));
    }


}