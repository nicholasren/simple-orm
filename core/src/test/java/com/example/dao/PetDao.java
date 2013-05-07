package com.example.dao;

import com.example.model.Pet;
import com.thoughtworks.orm.core.BaseDao;

public class PetDao extends BaseDao<Pet> {
    public PetDao(String databaseUrl) {
        super(databaseUrl);
    }
}
