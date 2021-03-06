package com.thoughtworks.orm.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.HasMany;
import com.thoughtworks.orm.annotations.Table;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.thoughtworks.orm.util.Lang.*;

class StatementGenerator {
    private static final String INSERTION_TEMPLATE = "INSERT INTO %s (%s) VALUES(%s)";
    private static final String SELECT_BY_CONDITION_TEMPLATE = "SELECT * FROM %s WHERE %s";
    private static final String UPDATE_TEMPLATE = "UPDATE %s SET %s WHERE id = %s";
    private static final String DELETE_TEMPLATE = "DELETE FROM %s WHERE id = %s";
    private static final String ALL_TEMPLATE = "SELECT * FROM %s";

    private static final String COLUMN_DELIMITER = ",";

    private java.sql.Connection connection;

    private Function<Field, String> getNameFunction = new Function<Field, String>() {
        @Override
        public String apply(Field field) {
            return field.getName();
        }
    };

    StatementGenerator(Connection connection) {
        this.connection = connection;
    }

    public List<PreparedStatement> insertHasManyField(Object obj) {
        List<PreparedStatement> preparedStatements = new ArrayList();
        Collection<Field> sortedAnnotatedField_hasMany = getSortedAnnotatedField_hasMany(obj.getClass());
        Collection<Object> values = getFieldValues(obj, sortedAnnotatedField_hasMany);
        Iterator<Object> iterator = values.iterator();
        while (iterator.hasNext()) {
            Object value = ((ArrayList) iterator.next()).get(0);
            Collection<Field> field = getSortedAnnotatedField(value.getClass());
            setForeignKeyValue(value, field, obj.getClass().getSimpleName().toLowerCase());
            preparedStatements.add(insertSimpleField(value));
        }
        return preparedStatements;
    }

    protected PreparedStatement insertSimpleField(Object obj) {
        String table = resolveTable(obj);
        Collection<Field> sortedAnnotatedField = getSortedAnnotatedField(obj.getClass());
        String sql = String.format(INSERTION_TEMPLATE, table, join(getFieldNames(sortedAnnotatedField), COLUMN_DELIMITER),
                join(getFieldValuePlaceHolders(obj), COLUMN_DELIMITER));

        info(sql);
        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement(sql);

            Object[] fieldValues = getFieldValues(obj, sortedAnnotatedField).toArray();

            for (int i = 0; i < fieldValues.length; i++) {
                preparedStatement.setObject(i + 1, fieldValues[i]);
            }

            return preparedStatement;
        } catch (SQLException e) {
            throw makeThrow("Exception encountered when generating insert statement: %s", stackTrace(e));
        }
    }

    private String resolveTable(Object obj) {
        return obj.getClass().getAnnotation(Table.class).value();
    }

    public List<PreparedStatement> update(Object obj) {
        String table = resolveTable(obj);
        List<PreparedStatement> preparedStatements = new ArrayList();

        Collection<Field> fieldsExceptId = filter(getSortedAnnotatedField(obj.getClass()), new Predicate<Field>() {
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

        String sql = String.format(UPDATE_TEMPLATE, table, join(setFields, COLUMN_DELIMITER), getId(obj));
        info(sql);
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
        preparedStatements.add(preparedStatement);
        return preparedStatements;
    }

    public PreparedStatement findById(Long id, Class entityClass) {
        return where("id = ?", new Object[]{id}, entityClass);
    }

    public PreparedStatement where(String condition, Object[] params, Class entityClass) {
        PreparedStatement preparedStatement;
        try {
            String sql = String.format(SELECT_BY_CONDITION_TEMPLATE, tableFor(entityClass), condition);
            info(sql);
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
        } catch (SQLException e) {
            throw makeThrow("Exception encountered when generating find by condition statement: %s", stackTrace(e));
        }
        return preparedStatement;
    }

    public <T> PreparedStatement all(Class<T> entityClass) {
        try {
            String sql = String.format(ALL_TEMPLATE, tableFor(entityClass));
            info(sql);
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw makeThrow("Exception encountered when generating all statement: %s", stackTrace(e));
        }
    }


    public List<PreparedStatement> delete(Long id, Class entityClass) {

        List<PreparedStatement> preparedStatements = new ArrayList<>();
        PreparedStatement preparedStatement;
        try {
            String sql = String.format(DELETE_TEMPLATE, tableFor(entityClass), id);
            info(sql);
            preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw makeThrow("Exception encountered when generating delete by id statement: %s", stackTrace(e));
        }

        preparedStatements.add(preparedStatement);
        return preparedStatements;
    }

    public static String join(Collection<String> items, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            builder.append(item).append(delimiter);
        }
        return builder.substring(0, builder.length() - delimiter.length());
    }

    private Collection<String> getFieldValuePlaceHolders(final Object t) {
        return transform(getSortedAnnotatedField(t.getClass()), new Function<Field, String>() {
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
                    Object value = field.get(t);
                    return field.getType().isEnum() ? ((Enum) value).name() : value;
                } catch (IllegalAccessException e) {
                    throw makeThrow("Got error when get field values %s", stackTrace(e));
                }
            }
        });
    }
    
    private void setForeignKeyValue(final Object t, Collection<Field> fields, final String foreignKeyName) {
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals(foreignKeyName + "_id")) {
                try {
                    field.set(t, 1L);
                } catch (IllegalAccessException e) {
                    makeThrow("Exception encountered when generating insert statement: %s", stackTrace(e));
                }
            }
        }
    }

    private Collection<Field> getSortedAnnotatedField(Class entityClass) {
        return Ordering.natural().onResultOf(getNameFunction).sortedCopy(getAnnotatedField(entityClass, Column.class));
    }

    private Collection<Field> getSortedAnnotatedField_hasMany(Class entityClass) {
        return Ordering.natural().onResultOf(getNameFunction).sortedCopy(getAnnotatedField(entityClass, HasMany.class));
    }

    private String tableFor(Class entityClass) {
        return ((Table) entityClass.getAnnotation(Table.class)).value();
    }

    public <T> List<PreparedStatement> insert(T obj) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();

        preparedStatements.add(insertSimpleField(obj));
        preparedStatements.addAll(insertHasManyField(obj));

        return preparedStatements;
    }
}