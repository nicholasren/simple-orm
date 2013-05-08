package com.thoughtworks.orm.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.thoughtworks.orm.util.Lang.makeThrow;
import static com.thoughtworks.orm.util.Lang.stackTrace;
import static java.sql.DriverManager.getConnection;

public class SessionFactory {

    private Connection connection;
    private StatementGenerator statementGenerator;

    public SessionFactory(String databaseUrl) {
        this.connection = getDBConnection(databaseUrl);
        this.statementGenerator = new StatementGenerator(this.connection);
    }

    //===============exposed API start====================================//
    public <T> T findById(Long id, Class<T> entityClass) {
        List<Object> list = where("id = ?", Arrays.asList(id).toArray(), entityClass);
        return list.isEmpty() ? null : (T) list.get(0);
    }

    public <T> List<T> where(String condition, Object[] params, Class entityClass) {
        ModelBuilder modelBuilder = new ModelBuilder(entityClass, this);
        return (List<T>) modelBuilder.build(statementGenerator.where(condition, params, entityClass));
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

    public <T> List<T> find(Criteria criteria, Class<T> entityClass) {
        return where(criteria.getCondition(), criteria.getParams(), entityClass);

    }

    public <T> List<T> all(Class<T> entityClass) {
        ModelBuilder modelBuilder = new ModelBuilder(entityClass, this);
        return (List<T>) modelBuilder.build(statementGenerator.all(entityClass));
    }
    //===============exposed API end====================================//

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

}
