package com.house.controller;

import com.house.model.House;
import com.house.service.HouseService;
import com.thoughtworks.mvc.annotation.Param;
import com.thoughtworks.mvc.annotation.Path;
import com.thoughtworks.mvc.core.Controller;
import com.thoughtworks.simpleframework.di.annotation.Component;
import com.thoughtworks.simpleframework.di.annotation.Inject;
import com.thoughtworks.simpleframework.di.core.Lifecycle;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component(lifecycle = Lifecycle.Transient)
@Path(url = "/house")
public class HouseController implements Controller {

    private Map<String, Object> modelMap = new HashMap<>();

    @Inject
    private HouseService service;

    @Path
    public String index() {
        modelMap.put("total", service.all().size());
        return "house/index";
    }

    @Path
    public String show(@Param("id") String id) throws SQLException {
        modelMap.put("house", service.get(id));
        return "house/show";
    }

    @Path
    public String create(@Param("house") House house) throws SQLException {
        House created = service.create(house);
        modelMap.put("house", created);
        return "house/show";
    }

    @Path(url = "new")
    public String fresh() {
        return "house/new";
    }

    @Override
    public Map<String, Object> getModelMap() {
        return this.modelMap;
    }
}
