package com.thoughtworks.orm.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.thoughtworks.orm.util.Lang.*;
import static java.sql.DriverManager.getConnection;

public class SessionFactory {

    private Connection connection;
    private StatementGenerator statementGenerator;

    public SessionFactory(String databaseUrl) {
        this.connection = getDBConnection(databaseUrl);
        this.statementGenerator = new StatementGenerator(this.connection);
    }


    public <T> T findById(Long id, Class<T> entityClass) {
        ModelBuilder modelBuilder = new ModelBuilder(entityClass, this);
        ResultSet resultSet = executeQuery(statementGenerator.findById(id, entityClass));
        return (T) modelBuilder.buildSingle(resultSet);
    }

    public <T> List<T> where(String condition, Object[] params, Class entityClass) {
        ModelBuilder modelBuilder = new ModelBuilder(entityClass, this);
        ResultSet resultSet = executeQuery(statementGenerator.where(condition, params, entityClass));
        return (List<T>) modelBuilder.buildCollections(resultSet);
    }

    public <T> void insert(T t) {
        executeUpdate(statementGenerator.insert(t));
    }

    public <T> void update(T t) {
        executeUpdate(statementGenerator.update(t));
    }


    public <T> void deleteById(Long id, Class<T> entityClass) {
        executeUpdate(statementGenerator.delete(id, entityClass));
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

    public <T> List<T> find(Criteria criteria, Class<T> entityClass) {
        ModelBuilder modelBuilder = new ModelBuilder(entityClass, this);
        ResultSet resultSet = executeQuery(statementGenerator.where(criteria.getCondition(), criteria.getParams(), entityClass));
        return (List<T>) modelBuilder.buildCollections(resultSet);

    }

    public <T> List<T> all(Class<T> entityClass) {
        ModelBuilder modelBuilder = new ModelBuilder(entityClass, this);
        ResultSet resultSet = executeQuery(statementGenerator.all(entityClass));
        return (List<T>) modelBuilder.buildCollections(resultSet);
    }
}
