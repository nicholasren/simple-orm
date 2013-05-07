package com.thoughtworks.orm.core;

import com.thoughtworks.orm.annotations.Column;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.thoughtworks.orm.util.Lang.*;

class ModelBuilder<T> {


    private final Class<T> entityClass;

    public ModelBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T build(ResultSet resultSet) {
        T model = null;
        try {
            if (resultSet.next()) {

                model = createModel(resultSet);
            }
        } catch (SQLException e) {
            throw makeThrow("Get error, stack trace are : %s", stackTrace(e));
        }

        return model;
    }

    public List<T> buildCollections(ResultSet resultSet) {
        List<T> models = new ArrayList<T>();
        try {
            while (resultSet.next()) {
                models.add(createModel(resultSet));
            }
        } catch (SQLException e) {
            throw makeThrow("Get error, stack trace are : %s", stackTrace(e));
        }
        return models;
    }


    private <T> void injectField(ResultSet resultSet, T model, Collection<Field> columnFields) throws SQLException, IllegalAccessException {
        for (Field field : columnFields) {
            Object value = resultSet.getObject(field.getName(), field.getType());
            field.setAccessible(true);
            field.set(model, value);
        }
    }


    private T createModel(ResultSet resultSet) {
        T model = instanceFor(entityClass);
        try {

            Collection<Field> columnFields = getAnnotatedField(entityClass, Column.class);
            injectField(resultSet, model, columnFields);
        } catch (Exception e) {
            throw makeThrow("Get error, stack trace are : %s", stackTrace(e));
        }
        return model;
    }
}
