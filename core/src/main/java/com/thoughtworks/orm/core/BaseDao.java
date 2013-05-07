package com.thoughtworks.orm.core;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import static com.thoughtworks.orm.util.Lang.*;
import static java.sql.DriverManager.getConnection;

public class BaseDao<T> {

    private final StatementGenerator statementGenerator;
    private Class<T> entityClass;


    private final String tableName;
    private Connection connection;

    public BaseDao(String databaseUrl) {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.tableName = entityClass.getAnnotation(Table.class).value();
        this.connection = getDBConnection(databaseUrl);
        this.statementGenerator = new StatementGenerator(entityClass, this.connection);
    }


    public T findById(Long id) {
        ResultSet resultSet = executeQuery(statementGenerator.findById(id));

        return (T) buildInstance(resultSet);
    }


    public void insert(T t) {
        executeUpdate(statementGenerator.insertion(t));
    }


    public void update(T t) throws SQLException, NoSuchFieldException, IllegalAccessException {
        executeUpdate(statementGenerator.update(t));
    }


    public void deleteById(int id) throws SQLException {
        String query = String.format("delete from %s where id = ?", tableName);

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);

        statement.executeUpdate();
    }

    private Object buildInstance(ResultSet resultSet) {
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

    private static <T> void injectField(ResultSet resultSet, T model, Collection<Field> columnFields) throws SQLException, IllegalAccessException {
        for (Field field : columnFields) {
            Object value = resultSet.getObject(field.getName(), field.getType());
            field.setAccessible(true);
            field.set(model, value);
        }
    }

    private Connection getDBConnection(String databaseUrl) {
        try {
            return getConnection(databaseUrl);
        } catch (SQLException e) {
            throw makeThrow("Get error on getting database connection, stack trace are : %s", stackTrace(e));
        }
    }

    private void executeUpdate(PreparedStatement preparedStatement) {
        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            makeThrow("Error encountered when executing insertion statement: %s", stackTrace(e));
        }
    }

    private ResultSet executeQuery(PreparedStatement preparedStatement) {
        ResultSet resultSet = null;
        try {
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            makeThrow("Error encountered when executing find by id statement: %s", stackTrace(e));
        }
        return resultSet;
    }
}
