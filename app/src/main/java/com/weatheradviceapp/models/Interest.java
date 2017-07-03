package com.weatheradviceapp.models;

import io.realm.RealmObject;

public class Interest extends RealmObject {
    private String tag;
    private String name;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
