package com.example;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.Table;

@Table("pets")
public class Pet {

    @Column
    private Long id;
    @Column
    private String name;
    @Column
    private String gender;
    @Column
    private Integer age;


    public String getName() {
        return name;
    }

    public String getGender() {
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

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
