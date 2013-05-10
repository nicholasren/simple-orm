package com.house.model;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.HasMany;
import com.thoughtworks.orm.annotations.Table;

import java.util.List;

@Table("houses")
public class House {

    @Column
    private Long id;

    public House(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Column
    private String name;

    @HasMany
    private List<Door> door;

    public House() {
    }


    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setDoor(List<Door> door) {
        this.door = door;
    }

    public List<Door> getDoor() {
        return door;
    }
}
