package com.thoughtworks.orm.core;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.thoughtworks.orm.util.Lang.*;
import static java.sql.DriverManager.getConnection;

public class BaseDao<T> {

    private final StatementGenerator statementGenerator;
    private Class<T> entityClass;
    private Connection connection;
    private ModelBuilder modelBuilder;

    public BaseDao(String databaseUrl) {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];

        this.connection = getDBConnection(databaseUrl);
        this.statementGenerator = new StatementGenerator(entityClass, this.connection);
        this.modelBuilder = new ModelBuilder(entityClass);
    }


    public T findById(Long id) {
        ResultSet resultSet = executeQuery(statementGenerator.findById(id));
        return (T) modelBuilder.build(resultSet);
    }

    public List<T> where(String condition, Object... params) {
        ResultSet resultSet = executeQuery(statementGenerator.where(condition, params));
        return (List<T>) modelBuilder.buildCollections(resultSet);
    }

    public void insert(T t) {
        executeUpdate(statementGenerator.insertion(t));
    }

    public void update(T t) {
        executeUpdate(statementGenerator.update(t));
    }


    public void deleteById(Long id) {
        executeUpdate(statementGenerator.delete(id));
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
            throw makeThrow("Error encountered when executing update statement: %s", stackTrace(e));
        }
    }

    private ResultSet executeQuery(PreparedStatement preparedStatement) {
        ResultSet resultSet;
        try {
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw makeThrow("Error encountered when executing query statement: %s", stackTrace(e));
        }
        return resultSet;
    }

}
