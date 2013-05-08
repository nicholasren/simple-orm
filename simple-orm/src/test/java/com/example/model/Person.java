package com.example.model;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.HasMany;
import com.thoughtworks.orm.annotations.Table;

import java.util.List;
import java.util.Set;

@Table("people")
public class Person {


    @Column
    private Long id;

    @Column
    private String name;

    @HasMany
    private List<Pet> pets;

    @HasMany
    private Set<Pet> petSet;

    @HasMany
    private Pet[] petArray;

    public Person() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public Set<Pet> getPetSet() {
        return petSet;
    }

    public Pet[] getPetArray() {
        return petArray;
    }
}
