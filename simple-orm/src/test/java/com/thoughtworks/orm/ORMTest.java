package com.thoughtworks.orm;

import com.thoughtworks.orm.core.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public class ORMTest {
    protected static Connection connection;
    protected static String databaseUrl = "jdbc:mysql://localhost:3306/orm?user=root";
    protected SessionFactory sessionFactory;

    @BeforeClass
    public static void beforeClass() throws Exception {
        connection = getConnection(databaseUrl);
    }

    @Before
    public void before() throws SQLException {
        sessionFactory = new SessionFactory(databaseUrl);
        connection.createStatement().execute("truncate pets");
        connection.createStatement().execute("truncate people");
    }

    @After
    public void after() throws SQLException {
        connection.createStatement().execute("truncate pets");
        connection.createStatement().execute("truncate people");
    }

    protected void preparePet(String name, String gender, Integer age, Long personId) {
        String insertSQL = "INSERT INTO pets values(%d, '%s', '%s', %d, %d)";
        try {
            connection.createStatement().executeUpdate(String.format(insertSQL, null, name, gender, age, personId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected String findPetNameById(Long id) throws SQLException {
        String selectSql = "SELECT name FROM pets WHERE id = %s";
        ResultSet result = null;
        try {
            result = connection.createStatement().executeQuery(String.format(selectSql, id));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result.next()) {
            return result.getString(1);
        } else {
            return "NULL";
        }
    }

    protected void preparePerson(Long id, String name) {
        String insertSQL = "INSERT INTO people values(%d, '%s')";
        try {
            connection.createStatement().executeUpdate(String.format(insertSQL, id, name));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
