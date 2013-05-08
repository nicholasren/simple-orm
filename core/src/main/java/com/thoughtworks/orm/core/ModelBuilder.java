package com.thoughtworks.orm.core;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.HasMany;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
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

    public List<T> buildCollections(PreparedStatement statement) {
        return createLazyCollection(statement);
    }


    private List<T> createLazyCollection(PreparedStatement statement) {
        LazyLoader lazy = new Lazy(statement);
        return (List<T>) Enhancer.create(List.class, lazy);
    }


    private T createLazyModel(ResultSet resultSet) {
        T model = (T) Enhancer.create(entityClass, new AssociationInterceptor(sessionFactory));
        try {
            injectField(resultSet, model);
        } catch (Exception e) {
            throw makeThrow("Get error, stack trace are : %s", stackTrace(e));
        }
        return model;
    }


    private <T> void injectField(ResultSet resultSet, T model) throws SQLException, IllegalAccessException {
        Collection<Field> columnFields = getAnnotatedField(entityClass, Column.class);
        for (Field field : columnFields) {
            Object value = resultSet.getObject(field.getName(), field.getType());
            field.setAccessible(true);
            field.set(model, value);
        }
    }

    public class Lazy implements LazyLoader {

        private PreparedStatement statement;

        public Lazy(PreparedStatement statement) {
            this.statement = statement;
        }

        @Override
        public List<T> loadObject() throws Exception {
            List<T> list = new ArrayList<T>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(createLazyModel(resultSet));
            }
            return list;
        }
    }

}
