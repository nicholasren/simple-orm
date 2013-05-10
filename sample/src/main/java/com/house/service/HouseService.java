package com.house.service;

import com.house.model.House;

import java.util.List;

public interface HouseService {
    public void create (House pet);

    House get(Long id);

    List<House> all();
}
