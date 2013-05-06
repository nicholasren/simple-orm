package com.thoughtworks.orm;

import com.example.Pet;
import com.example.dao.PetDao;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class MappingTest {

    private static Connection connection;
    private static String databaseUrl = "jdbc:mysql://localhost:3306/orm?user=root";
    private static PetDao petDao;

    @BeforeClass
    public static void beforeClass() throws Exception {
        connection = getConnection(databaseUrl);
        petDao = new PetDao(databaseUrl);
    }


    @Test
    public void should_find_object_by_id() throws SQLException {
        String insertSQL = "INSERT INTO pets values(%d,'%s', '%s', %d)";
        connection.createStatement().executeUpdate(String.format(insertSQL, 1, "Doudou", "Female", 2));

        Pet pet = petDao.findById(1L);

        assertThat(pet.getName(), equalTo("Doudou"));
        assertThat(pet.getGender(), equalTo("Female"));
        assertThat(pet.getAge(), equalTo(2));
    }

    @Test
    public void should_delete_object_by_id() throws SQLException {
        String insertSQL = "INSERT INTO pets values(%d,'%s', '%s', %d)";
        connection.createStatement().executeUpdate(String.format(insertSQL, 1, "Doudou", "Female", 2));

        Pet pet = petDao.findById(1L);

        assertThat(pet.getName(), equalTo("Doudou"));
        assertThat(pet.getGender(), equalTo("Female"));
        assertThat(pet.getAge(), equalTo(2));

        petDao.deleteById(1);

        Pet pet1 = petDao.findById(1L);
        assertNull(pet1);
    }

    @Test
    public void should_update_object() throws SQLException, NoSuchFieldException, IllegalAccessException {
        String insertSQL = "INSERT INTO pets values(%d,'%s', '%s', %d)";
        connection.createStatement().executeUpdate(String.format(insertSQL, 1, "Doudou", "Female", 2));

        Pet pet = petDao.findById(1L);
        pet.setAge(19);
        pet.setName("James");
        petDao.update(pet);

        Pet pet1 = petDao.findById(1L);

        assertThat(pet1.getName(), equalTo("James"));
        assertThat(pet1.getGender(), equalTo("Female"));
        assertThat(pet1.getAge(), equalTo(19));
    }

    @Test
    public void should_insert_object_to_database() throws SQLException, NoSuchFieldException, IllegalAccessException {

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setAge(19);
        pet.setName("James");
        pet.setGender("Female");
        petDao.insert(pet);

        Pet pet1 = petDao.findById(1L);

        assertThat(pet1.getName(), equalTo("James"));
        assertThat(pet1.getGender(), equalTo("Female"));
        assertThat(pet1.getAge(), equalTo(19));
    }

    @After
    public void tearDown() throws SQLException {
        connection.createStatement().execute("truncate pets");
    }
}
