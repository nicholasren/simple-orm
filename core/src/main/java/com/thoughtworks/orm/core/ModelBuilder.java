package com.thoughtworks.orm.core;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.HasMany;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import static com.thoughtworks.orm.util.Lang.*;

class ModelBuilder<T> {


    private final Class<T> entityClass;

    public ModelBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Object build(ResultSet resultSet) {
        Object model = null;
        try {
            if (resultSet.next()) {

                model = instanceFor(entityClass);

                Collection<Field> columnFields = getAnnotatedField(entityClass, Column.class);
                injectField(resultSet, model, columnFields);
            }
        } catch (Exception e) {
            throw makeThrow("Get error, stack trace are : %s", stackTrace(e));
        }
        return model;
    }


    private <T> void injectField(ResultSet resultSet, T model, Collection<Field> columnFields) throws SQLException, IllegalAccessException {
        for (Field field : columnFields) {
            Object value = resultSet.getObject(field.getName(), field.getType());
            field.setAccessible(true);
            field.set(model, value);
        }
    }

}
