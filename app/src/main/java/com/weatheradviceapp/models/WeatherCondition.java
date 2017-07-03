package com.weatheradviceapp.models;

import android.text.format.DateUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.survivingwithandroid.weather.lib.model.WeatherHourForecast;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.Ignore;

public class WeatherCondition extends RealmObject {
    private String serializedForecast;
    private Date fetchDate;
    private double locationLat;
    private double locationLng;

    @Ignore
    private WeatherHourForecast forecast;

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

    public WeatherHourForecast getForecast() {

        if (this.forecast == null) {
            this.forecast = gson().fromJson(this.serializedForecast, WeatherHourForecast.class);
        }

        return this.forecast;
    }

    public void setForecast(WeatherHourForecast forecast) {
        this.forecast = forecast;
        this.serializedForecast = gson().toJson(forecast);
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

    public Weather getWeather() {
        // We fetched the weather per hour. Calculate the time difference
        // between the fetch date and the current date.
        long date_difference = new Date().getTime() - getFetchDate().getTime();
        int hour = (int)(date_difference / DateUtils.HOUR_IN_MILLIS);

        // If our weather forecast was refreshed later than forecast
        // availability, use the last known weather.
        if (getForecast().hoursForecast.size() < hour) {
            hour = (getForecast().hoursForecast.size() - 1);
        }

        return getForecast().getHourForecast(hour).weather;
    }
}
