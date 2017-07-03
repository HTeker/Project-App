package com.weatheradviceapp.models;

import io.realm.RealmObject;

/**
 * Created by 168580 on 6/16/2017.
 */

public class Network extends RealmObject {
    private String name;

    public Network(){}

    public Network(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
