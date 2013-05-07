package com.thoughtworks.orm.core;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.HasMany;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.thoughtworks.orm.util.Lang.*;

class ModelBuilder<T> {


    private final Class<T> entityClass;
    private final SessionFactory sessionFactory;

    public ModelBuilder(Class<T> entityClass, SessionFactory sessionFactory) {
        this.entityClass = entityClass;
        this.sessionFactory = sessionFactory;
    }

    public T buildSingle(ResultSet resultSet) {
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


    private T createModel(ResultSet resultSet) {
        T model = instanceFor(entityClass);
        try {
            Collection<Field> columnFields = getAnnotatedField(entityClass, Column.class);
            injectField(resultSet, model, columnFields);

            Collection<Field> associatedField = getAnnotatedField(entityClass, HasMany.class);
            injectAssociation(model, associatedField);
        } catch (Exception e) {
            throw makeThrow("Get error, stack trace are : %s", stackTrace(e));
        }
        return model;
    }

    private void injectAssociation(T model, Collection<Field> associatedField) throws IllegalAccessException {
        for (Field field : associatedField) {
            Class targetClass = resolveTargetClass(field);

            List list = sessionFactory.where(foreignKey(model) + " = ?", new Long[]{getId(model)}, targetClass);
            field.setAccessible(true);
            field.set(model, list);
        }
    }

    private String foreignKey(T model) {
        return model.getClass().getSimpleName().toLowerCase() + "_id";
    }

    private Class resolveTargetClass(Field field) {
        return (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    private <T> void injectField(ResultSet resultSet, T model, Collection<Field> columnFields) throws SQLException, IllegalAccessException {
        for (Field field : columnFields) {
            Object value = resultSet.getObject(field.getName(), field.getType());
            field.setAccessible(true);
            field.set(model, value);
        }
    }
}
