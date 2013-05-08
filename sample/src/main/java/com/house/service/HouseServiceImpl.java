package com.house.service;

import com.house.model.House;
import com.thoughtworks.simpleframework.di.annotation.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class HouseServiceImpl implements HouseService {

    private static AtomicLong idSequence = new AtomicLong();
    private Map<String, House> pets = new HashMap<>();

    @Override
    public House create(House house) {
        house.setId(String.valueOf(idSequence.incrementAndGet()));
        pets.put(house.getId(), house);
        return house;
    }

    @Override
    public House get(String id) {
        House pet = pets.get(id);
        if (pet == null) {
            pet = new House("Dummy", "Doudou");
        }
        return pet;
    }
}
