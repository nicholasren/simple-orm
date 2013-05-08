package com.thoughtworks.orm;

import com.example.model.Pet;
import com.thoughtworks.orm.core.Criteria;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SimpleMappingTest extends ORMTest {

    @Test
    public void should_find_object_by_id() {
        preparePet(1L, "Test1 Doudou", "Female", 2, 1L);

        Pet pet = sessionFactory.findById(1L, Pet.class);

        assertThat(pet.getName(), equalTo("Test1 Doudou"));
        assertThat(pet.getGender(), equalTo("Female"));
        assertThat(pet.getAge(), equalTo(2));
    }

    @Test
    public void should_return_all(){
        preparePet(1L, "Test2 Doudou", "Female", 2, 1L);
        preparePet(2L, "Tom", "Female", 2, 1L);
        List<Pet> pets = sessionFactory.all(Pet.class);

        assertThat(pets.size(), equalTo(2));
    }

    @Test
    public void should_find_by_condition() {
        preparePet(1L, "Test3_1 Doudou", "Female", 2, 1L);
        preparePet(2L, "Test3_2 Doudou", "Female", 2, 1L);
        preparePet(3L, "Test3_3 Doudou", "Female", 2, 1L);

        List<Pet> pets = sessionFactory.where("person_id = ?", new Object[]{1L}, Pet.class);

        assertThat(pets.isEmpty(), is(false));
        assertThat(pets.get(0).getGender(), equalTo("Female"));
    }

    @Test
    public void should_delete_object_by_id() throws SQLException {
        preparePet(1L, "Test4 Doudou", "Female", 2, 1L);

        Pet pet = sessionFactory.findById(1L, Pet.class);

        assertThat(pet.getName(), equalTo("Test4 Doudou"));
        assertThat(pet.getGender(), equalTo("Female"));
        assertThat(pet.getAge(), equalTo(2));

        sessionFactory.deleteById(1L, Pet.class);

        String petName = findPetNameById(1L);
        assertThat(petName, is("NULL"));
    }

    @Test
    public void should_update_object() throws SQLException, NoSuchFieldException, IllegalAccessException {
        preparePet(1L, "Test5 Doudou", "Female", 2, 1L);

        Pet pet = new Pet();
        pet.setId(1L);
        pet.setGender("Female");
        pet.setAge(19);
        pet.setName("James");
        sessionFactory.update(pet);

        String petName = findPetNameById(1L);

        assertThat(petName, equalTo("James"));
    }

    @Test
    public void should_insert_object_to_database() throws SQLException, NoSuchFieldException, IllegalAccessException {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName("Test6 James");
        pet.setGender("Female");
        pet.setAge(19);
        sessionFactory.insert(pet);

        Pet pet1 = sessionFactory.findById(1L, Pet.class);

        assertThat(pet1.getName(), equalTo("Test6 James"));
        assertThat(pet1.getGender(), equalTo("Female"));
        assertThat(pet1.getAge(), equalTo(19));
    }

    @Test
    public void should_query_records_by_criteria() {
        preparePet(1L, "Test7 James", "Female", 19, 1L);
        preparePet(1L, "Test7 Ben", "Male", 20, 1L);
        preparePet(1L, "Test7 JP", "Male", 20, 1L);
        preparePet(1L, "Test7 Luke", "Female", 20, 1L);

        Criteria criteria = new Criteria();
        criteria.eq("age", 20).and().eq("name", "Test7 Luke");
        List<Pet> pet1 = sessionFactory.find(criteria, Pet.class);
        assertThat(pet1.get(0).getName(), equalTo("Test7 Luke"));
    }

}
