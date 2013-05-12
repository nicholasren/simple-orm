package com.thoughtworks.orm.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.HasMany;
import com.thoughtworks.orm.util.Lang;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.google.common.collect.Collections2.transform;
import static com.thoughtworks.orm.util.Lang.*;

class ModelBuilder<T> {

    private final Class<T> entityClass;
    private final StatementGenerator statementGenerator;

    public ModelBuilder(Class<T> entityClass, StatementGenerator statementGenerator) {
        this.entityClass = entityClass;
        this.statementGenerator = statementGenerator;
    }

    public List<T> build(PreparedStatement statement) {
        List<T> models = new ArrayList();
        try {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                models.add(createModel(resultSet, entityClass));
            }
        } catch (SQLException e) {
            throw makeThrow("Got exception when building model, %s", stackTrace(e));
        }

        for (Field association : Lang.getAnnotatedField(entityClass, HasMany.class)) {
            assembleAssociation(models, association);
        }

        return models;
    }

    private void assembleAssociation(List<T> models, Field association) {
        Map<Long, Collection> associationMap = loadAssociatedValues(models, association);

        for (final Map.Entry<Long, Collection> entry : associationMap.entrySet()) {

            T t = Iterables.find(models, new Predicate<T>() {
                @Override
                public boolean apply(T input) {
                    return entry.getKey().equals(getId(input));
                }
            });

            try {
                association.setAccessible(true);
                association.set(t, entry.getValue());
            } catch (IllegalAccessException e) {
                throw makeThrow(stackTrace(e));
            }
        }
    }

    private Map<Long, Collection> loadAssociatedValues(List models, Field association) {

        Collection<String> ids = transform(models, new Function<T, String>() {
            @Override
            public String apply(T input) {
                return getId(input).toString();
            }
        });

        Map<Long, Collection> associationMap = new HashMap<>();

        Class targetClass = targetClass(association);
        Class containerClass = association.getType();

        ResultSet resultSet = getAssociationResultSet(ids, targetClass);

        try {

            while (resultSet.next()) {

                Long parentId = resultSet.getLong(foreignKey());
                Object o = createModel(resultSet, targetClass);

                Collection associationCollection = ensureCollectionExists(associationMap, containerClass, parentId);

                associationCollection.add(o);
            }
        } catch (SQLException e) {
            throw makeThrow("Error on build associations: %s", stackTrace(e));
        }
        return associationMap;
    }

    private ResultSet getAssociationResultSet(Collection<String> ids, Class targetClass) {
        PreparedStatement associationStatement = statementGenerator.where(foreignKey() + " in (?)", new String[]{StatementGenerator.join(ids, ",")}, targetClass);
        try {
            return associationStatement.executeQuery();
        } catch (SQLException e) {
            throw makeThrow(stackTrace(e));
        }
    }

    private Collection ensureCollectionExists(Map<Long, Collection> associationMap, Class containerClass, Long parentId) {
        Collection associationCollection = associationMap.get(parentId);
        if (associationCollection == null) {
            associationCollection = containerClass.equals(Set.class) ? new HashSet() : new ArrayList();
            associationMap.put(parentId, associationCollection);
        }
        return associationCollection;
    }

    private String foreignKey() {
        return entityClass.getSimpleName().toLowerCase() + "_id";
    }

    private T createModel(ResultSet resultSet, Class clazz) {
        T model = instanceFor(clazz);
        try {
            injectField(resultSet, model);
        } catch (Exception e) {
            throw makeThrow("Get error, stack trace are : %s", stackTrace(e));
        }
        return model;
    }

    private <T> void injectField(ResultSet resultSet, T model) throws SQLException, IllegalAccessException {
        Collection<Field> columnFields = getAnnotatedField(model.getClass(), Column.class);
        for (Field field : columnFields) {
            field.setAccessible(true);
            if (field.getType().isEnum()) {
                Enum value = getEnumValue(resultSet, field);
                field.set(model, value);
            } else {
                Object value = resultSet.getObject(field.getName(), field.getType());
                field.set(model, value);
            }
        }
    }

    private Enum getEnumValue(ResultSet resultSet, Field field) throws SQLException {
        String strValue = resultSet.getObject(field.getName(), String.class);
        return Enum.valueOf((Class<Enum>) field.getType(), strValue);
    }

    private Class targetClass(Field field) {
        return (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

}
