package com.thoughtworks.orm.core;

import java.util.ArrayList;
import java.util.List;

public class Criteria {
    private String condition ="";
    List<Object> params = new ArrayList<Object>();

    public Criteria eq(String name, Object value) {
        this.condition += name + " = " + "?";
        this.params.add(value);
        return this;
    }

    public Criteria and() {
        this.condition += " and ";
        return this;
    }

    public String getCondition() {
        return condition;
    }

    public Object[] getParams() {
        return params.toArray();
    }
}
