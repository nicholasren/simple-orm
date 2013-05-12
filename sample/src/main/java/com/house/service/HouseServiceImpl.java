package com.house.service;

import com.house.model.Door;
import com.house.model.House;
import com.thoughtworks.orm.core.SessionFactory;
import com.thoughtworks.simpleframework.di.annotation.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.sql.DriverManager.getConnection;

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
    public House get(Long id) {
        return sessionFactory.findById(id, House.class);

    }

    @Override
    public List<House> all() {
        return sessionFactory.all(House.class);
    }

    @Override
    public void create(House house) {
        house.setId(idSequence.incrementAndGet());
        sessionFactory.insert(house);
        Door door = house.getDoor().get(0);
        sessionFactory.insert(door);
    }

    protected void prepareDoor(Long id, Long houseId) {
        String insertSQL = "UPDATE doors SET house_id = %s WHERE id = %s";
        try {
            Connection statement = getConnection(databaseUrl);
            statement.createStatement().executeUpdate(String.format(insertSQL, id, houseId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
