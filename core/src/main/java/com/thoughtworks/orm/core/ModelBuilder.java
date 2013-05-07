package com.thoughtworks.orm.core;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.HasMany;
import net.sf.cglib.proxy.Enhancer;

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
        Enhancer en = new Enhancer();
        en.setSuperclass(entityClass);
        en.setCallback(new GetterInterceptor(sessionFactory));

        T model = (T) en.create();
        try {
            Collection<Field> columnFields = getAnnotatedField(entityClass, Column.class);
            injectField(resultSet, model, columnFields);
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
