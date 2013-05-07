package com.thoughtworks.orm;

import com.example.model.Person;
import com.example.model.Pet;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class OneToManyTest extends ORMTest {

    @Test
    public void should_return_pets_belongs_to_person() {
        preparePerson(1L, "jim");
        preparePet(1L, "p1", "female", 1, 1L);
        preparePet(2L, "p2", "female", 1, 1L);
        preparePet(3L, "p3", "female", 1, 1L);


        Person person = sessionFactory.findById(1L, Person.class);
        List<Pet> pets = person.getPets();

        assertThat(person, notNullValue());
        assertThat(pets.size(), equalTo(3));
    }


}
