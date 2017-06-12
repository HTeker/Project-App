package com.weatheradviceapp.models;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.Weather;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class UserCalendarEvent extends RealmObject {
    private Date eventBeginDate;
    private Date eventEndDate;
    private double locationLat;
    private double locationLng;
    private String locationTitle;
    private String serializedWeather;

    @Ignore
    private Weather weather;

    long eventID = 0;
    long instanceID = 0;
    String title;

    public Date getEventBeginDate() {
        return eventBeginDate;
    }

    public void setEventBeginDate(Date eventBeginDate) {
        this.eventBeginDate = eventBeginDate;
    }

    public Date getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(Date eventEndDate) {
        this.eventEndDate = eventEndDate;
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

    public String getLocationTitle() {
        return locationTitle;
    }

    public void setLocationTitle(String locationTitle) {
        this.locationTitle = locationTitle;
    }

    public long getEventID() {
        return eventID;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    public long getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(long instanceID) {
        this.instanceID = instanceID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Weather getWeather() {

        if (this.weather == null) {
            this.weather = gson().fromJson(this.serializedWeather, Weather.class);
        }

        return this.weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
        this.serializedWeather = gson().toJson(weather);
    }

    private Gson gson() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeSpecialFloatingPointValues();
        return builder.create();
    }
}
