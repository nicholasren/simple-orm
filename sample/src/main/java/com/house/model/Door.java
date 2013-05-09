package com.house.model;

import com.thoughtworks.orm.annotations.Column;

public class Door {
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

}
