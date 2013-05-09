package com.house.service;

import com.house.model.House;

import java.util.List;

public interface HouseService {
    public House create (House pet);

    House get(String id);

    List<House> all();
}
