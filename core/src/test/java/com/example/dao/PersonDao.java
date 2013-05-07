package com.example.dao;

import com.example.model.Person;
import com.thoughtworks.orm.core.BaseDao;

public class PersonDao extends BaseDao<Person> {
    public PersonDao(String databaseUrl) {
        super(databaseUrl);
    }
}
