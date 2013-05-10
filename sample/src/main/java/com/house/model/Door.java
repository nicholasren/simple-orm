package com.house.model;

import com.thoughtworks.orm.annotations.Column;
import com.thoughtworks.orm.annotations.Table;

@Table("doors")
public class Door {
    @Column
    private Long id;

    @Column
    private Integer width;

    @Column
    private Integer height;

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
