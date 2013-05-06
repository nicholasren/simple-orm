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

    private Class<T> entityClass;


    private final String tableName;
    private Connection connection;

    public BaseDao(String databaseUrl) {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.tableName = entityClass.getAnnotation(Table.class).value();
        this.connection = getDBConnection(databaseUrl);
    }

    private Connection getDBConnection(String databaseUrl) {
        try {
            return getConnection(databaseUrl);
        } catch (SQLException e) {
            throw makeThrow("Get error on getting database connection, stack trace are : %s", stackTrace(e));
        }
    }


    public T findById(Long id) {
        String tableName = entityClass.getAnnotation(Table.class).value();
        String query = String.format("select * from %s where id = %s", tableName, id);

        ResultSet resultSet = getResultSet(query);

        return (T) buildInstance(resultSet);
    }


    public void update(T t) throws SQLException, NoSuchFieldException, IllegalAccessException {
        Field idField = t.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        Object id = idField.get(t);
        for (Field i : t.getClass().getDeclaredFields()) {
            if (i.getName() != "id") {
                i.setAccessible(true);
                Object value = i.get(t);

                updateDatabase((Long) id, i.getName(), String.valueOf(value));
            }
        }
    }

    public void insert(T t) throws IllegalAccessException, SQLException {
        String insertionSql = prepareInsertionSql(t);
        connection.createStatement().executeUpdate(insertionSql);
    }


    public void deleteById(int id) throws SQLException {
        String query = String.format("delete from %s where id = ?", tableName);

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);

        statement.executeUpdate();
    }

    private void updateDatabase(Long id, String name, String value) throws SQLException {
        String query = String.format("update %s set %s = ? where id = %s", tableName, name, id);
        PreparedStatement preparedStmt = connection.prepareStatement(query);
        preparedStmt.setString(1, value);
        preparedStmt.executeUpdate();
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

    private ResultSet getResultSet(String query) {
        try {
            return connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            throw makeThrow("Get error, stack trace are : %s", stackTrace(e));
        }
    }

    private String prepareInsertionSql(T t) throws IllegalAccessException {
        String insertSQL = "INSERT INTO pets values(%s)";
        String insertValue = "";
        int count = 0;
        for (Field field : getAnnotatedField(entityClass, Column.class)) {
            count++;

            field.setAccessible(true);
            Object value = field.get(t);
            if (field.getType() == Integer.class) {
                insertValue += String.valueOf(value);
            } else {
                insertValue += "'" + String.valueOf(value) + "'";
            }

            if (count != t.getClass().getDeclaredFields().length) {
                insertValue += ", ";
            }
        }
        return String.format(insertSQL, insertValue);
    }


}
