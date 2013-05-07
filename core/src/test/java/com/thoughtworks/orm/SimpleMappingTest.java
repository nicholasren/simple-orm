package com.thoughtworks.orm;

import com.example.Pet;
import com.example.dao.PetDao;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static java.sql.DriverManager.getConnection;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class SimpleMappingTest extends ORMTest {

    private static PetDao petDao;

    @Before
    public void before() {
        petDao = new PetDao(databaseUrl);
    }

    @Test
    public void should_find_object_by_id() {
        preparePet(1L, "Doudou", "Female", 2, 1L);

        Pet pet = petDao.findById(1L);

        assertThat(pet.getName(), equalTo("Doudou"));
        assertThat(pet.getGender(), equalTo("Female"));
        assertThat(pet.getAge(), equalTo(2));
    }

    @Test
    public void should_find_by_condition() {
        preparePet(1L, "p1", "Female", 2, 1L);
        preparePet(2L, "p2", "Female", 2, 1L);
        preparePet(3L, "p3", "Female", 2, 1L);

        List<Pet> pets = petDao.where("person_id = ?", 1L);

        assertThat(pets.isEmpty(), is(false));
        assertThat(pets.get(0).getGender(), equalTo("Female"));
    }

    @Test
    public void should_delete_object_by_id() throws SQLException {
        preparePet(1L, "Doudou", "Female", 2, 1L);

        Pet pet = petDao.findById(1L);

        assertThat(pet.getName(), equalTo("Doudou"));
        assertThat(pet.getGender(), equalTo("Female"));
        assertThat(pet.getAge(), equalTo(2));

        petDao.deleteById(1L);

        Pet pet1 = petDao.findById(1L);
        assertNull(pet1);
    }

    @Test
    public void should_update_object() throws SQLException, NoSuchFieldException, IllegalAccessException {
        preparePet(1L, "Doudou", "Female", 2, 1L);

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

}
