package com.weatheradviceapp.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.Ignore;

public class WeatherCondition extends RealmObject {
    private String serializedWeather;
    private Date fetchDate;
    private double locationLat;
    private double locationLng;

    @Ignore
    private CurrentWeather weather;

    public Date getFetchDate() {
        return fetchDate;
    }

    public void setFetchDate(Date fetchDate) {
        this.fetchDate = fetchDate;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(double locationLng) {
        this.locationLng = locationLng;
    }

    public CurrentWeather getWeather() {

        if (this.weather == null) {
            this.weather = gson().fromJson(this.serializedWeather, CurrentWeather.class);
        }

        return this.weather;
    }

    public void setWeather(CurrentWeather weather) {
        this.weather = weather;
        this.serializedWeather = gson().toJson(weather);
    }

    private Gson gson() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeSpecialFloatingPointValues();
        return builder.create();
    }

    public static WeatherCondition getLatestWeatherCondition() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<WeatherCondition> list = realm.where(WeatherCondition.class).findAllSorted("fetchDate", Sort.DESCENDING);
        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }
}
