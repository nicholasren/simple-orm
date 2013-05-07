package com.example.model;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.HasMany;
import com.thoughtworks.orm.annotations.Table;

import java.util.List;

@Table("people")
public class Person {

    @Column
    private Long id;

    @Column
    private String name;

    @HasMany
    private List<Pet> pets;

    public Person() {
    }

    public List<Pet> getPets() {
        return pets;
    }
}
