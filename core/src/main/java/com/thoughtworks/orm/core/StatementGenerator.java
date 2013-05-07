package com.thoughtworks.orm.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.Table;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.thoughtworks.orm.util.Lang.*;

class StatementGenerator {
    private static final String INSERTION_TEMPLATE = "INSERT INTO %s (%s) values(%s)";
    private static final String COLUMN_DELIMITER = ",";
    private static final String SELECT_BY_ID_TEMPLATE = "select * from %s where id = %s";
    private static final String UPDATE_TEMPLATE = "UPDATE %s set %s where id = %s";
    private static final String DELETE_TEMPLATE = "delete from %s where id = %s";

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
        String sql = String.format(INSERTION_TEMPLATE, table, join(getFieldNames(getSortedAnnotatedField()), COLUMN_DELIMITER),
                join(getFieldValuePlaceHolders(obj, getSortedAnnotatedField()), COLUMN_DELIMITER));

        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement(sql);

            Object[] fieldValues = getFieldValues(obj, getSortedAnnotatedField()).toArray();

            for (int i = 0; i < fieldValues.length; i++) {
                preparedStatement.setObject(i + 1, fieldValues[i]);
            }

        } catch (SQLException e) {
            throw makeThrow("Exception encountered when generating insertion statement: %s", stackTrace(e));
        }
        return preparedStatement;
    }

    public PreparedStatement update(Object obj) {
        Long id;
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            id = (Long) idField.get(obj);
        } catch (Exception e) {
            throw makeThrow("Exception encountered when get id of obj, : %s", stackTrace(e));
        }

        Collection<Field> fieldsExceptId = filter(getSortedAnnotatedField(), new Predicate<Field>() {
            @Override
            public boolean apply(Field input) {
                return !input.getName().equals("id");
            }
        });

        Collection<String> setFields = transform(getFieldNames(fieldsExceptId), new Function<String, String>() {
            @Override
            public String apply(java.lang.String name) {
                return new StringBuilder(name).append(" = ? ").toString();
            }
        });

        String sql = String.format(UPDATE_TEMPLATE, table, join(setFields, COLUMN_DELIMITER), id);
        System.out.println(sql);
        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement(sql);

            Object[] fieldValues = getFieldValues(obj, fieldsExceptId).toArray();

            for (int i = 0; i < fieldValues.length; i++) {
                preparedStatement.setObject(i + 1, fieldValues[i]);
            }

        } catch (SQLException e) {
            throw makeThrow("Exception encountered when generating updating statement: %s", stackTrace(e));
        }
        return preparedStatement;
    }

    public PreparedStatement findById(Long id) {

        PreparedStatement preparedStatement;
        try {
            String sql = String.format(SELECT_BY_ID_TEMPLATE, table, id);
            preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw makeThrow("Exception encountered when generating find by id statement: %s", stackTrace(e));
        }
        return preparedStatement;
    }

    public PreparedStatement delete(Long id) {
        PreparedStatement preparedStatement;
        try {
            String sql = String.format(DELETE_TEMPLATE, table, id);
            preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw makeThrow("Exception encountered when generating delete by id statement: %s", stackTrace(e));
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

    private Collection<String> getFieldValuePlaceHolders(final Object t, Collection<Field> fields) {
        return transform(getSortedAnnotatedField(), new Function<Field, String>() {
            @Override
            public String apply(java.lang.reflect.Field field) {
                return "?";
            }
        });
    }

    private Collection<String> getFieldNames(Collection<Field> fields) {
        return transform(fields, new Function<Field, String>() {
            @Override
            public String apply(java.lang.reflect.Field field) {
                return field.getName();
            }
        });
    }

    private Collection<Object> getFieldValues(final Object t, Collection<Field> fields) {
        return transform(fields, new Function<Field, Object>() {
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