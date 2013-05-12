package com.example.model;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.Table;

@Table("pets")
public class Pet {


    @Column
    private Long id;
    @Column
    private String name;
    @Column
    private Gender gender;
    @Column
    private Integer age;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Gender getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

