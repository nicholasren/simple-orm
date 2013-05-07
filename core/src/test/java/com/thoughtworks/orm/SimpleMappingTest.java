package com.thoughtworks.orm;

import com.example.model.Pet;
import com.thoughtworks.orm.core.Criteria;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class SimpleMappingTest extends ORMTest {


    @Test
    public void should_find_object_by_id() {
        preparePet(1L, "Doudou", "Female", 2, 1L);

        Pet pet = sessionFactory.findById(1L, Pet.class);

        assertThat(pet.getName(), equalTo("Doudou"));
        assertThat(pet.getGender(), equalTo("Female"));
        assertThat(pet.getAge(), equalTo(2));
    }

    @Test
    public void should_find_by_condition() {
        preparePet(1L, "p1", "Female", 2, 1L);
        preparePet(2L, "p2", "Female", 2, 1L);
        preparePet(3L, "p3", "Female", 2, 1L);

        List<Pet> pets = sessionFactory.where("person_id = ?", new Object[]{1L}, Pet.class);

        assertThat(pets.isEmpty(), is(false));
        assertThat(pets.get(0).getGender(), equalTo("Female"));
    }

    @Test
    public void should_delete_object_by_id() throws SQLException {
        preparePet(1L, "Doudou", "Female", 2, 1L);

        Pet pet = sessionFactory.findById(1L, Pet.class);

        assertThat(pet.getName(), equalTo("Doudou"));
        assertThat(pet.getGender(), equalTo("Female"));
        assertThat(pet.getAge(), equalTo(2));

        sessionFactory.deleteById(1L, Pet.class);

        Pet pet1 = sessionFactory.findById(1L, Pet.class);
        assertNull(pet1);
    }

    @Test
    public void should_update_object() throws SQLException, NoSuchFieldException, IllegalAccessException {
        preparePet(1L, "Doudou", "Female", 2, 1L);

        Pet pet = sessionFactory.findById(1L, Pet.class);
        pet.setAge(19);
        pet.setName("James");
        sessionFactory.update(pet);

        Pet pet1 = sessionFactory.findById(1L, Pet.class);

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
        sessionFactory.insert(pet);

        Pet pet1 = sessionFactory.findById(1L, Pet.class);

        assertThat(pet1.getName(), equalTo("James"));
        assertThat(pet1.getGender(), equalTo("Female"));
        assertThat(pet1.getAge(), equalTo(19));
    }

    @Test
    public void should_query_records_by_criteria() {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setAge(19);
        pet.setName("James");
        pet.setGender("Female");

        sessionFactory.insert(pet);
        pet.setAge(20);
        pet.setName("Ben");
        pet.setGender("Male");
        sessionFactory.insert(pet);

        pet.setAge(20);
        pet.setName("JP");
        pet.setGender("Male");
        sessionFactory.insert(pet);

        pet.setAge(20);
        pet.setName("Luke");
        pet.setGender("Male");
        sessionFactory.insert(pet);

        Criteria criteria = new Criteria();
        criteria.eq("age", 20).and().eq("name", "Luke");
        List<Pet> pet1 = sessionFactory.find(criteria, Pet.class);
        assertThat(pet1.get(0).getName(), equalTo("Luke"));
    }

}
