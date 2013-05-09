package com.house.service;

import com.house.model.House;
import com.thoughtworks.orm.core.SessionFactory;
import com.thoughtworks.simpleframework.di.annotation.Component;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class HouseServiceImpl implements HouseService {

    private static AtomicLong idSequence = new AtomicLong();
    protected static Connection connection;
    protected SessionFactory sessionFactory;
    protected static String databaseUrl = "jdbc:mysql://localhost:3306/orm?user=root";

    public HouseServiceImpl() {
        this.sessionFactory = new SessionFactory(databaseUrl);
    }

    @Override
    public House get(String id) {
        return sessionFactory.findById(1L, House.class);
    }

    @Override
    public List<House> all() {
        return sessionFactory.all(House.class);
    }

    @Override
    public House create(House house) {
        house.setId(idSequence.incrementAndGet());
        sessionFactory.insert(house);
        return house;
    }
}
