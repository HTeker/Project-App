package com.weatheradviceapp.jobs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.HourForecast;
import com.survivingwithandroid.weather.lib.model.WeatherHourForecast;
import com.weatheradviceapp.models.WeatherCondition;

import java.util.Date;

import io.realm.Realm;

public class DemoWeatherJob extends DemoJob {

    public static final String TAG = "demo_weather_job_tag";
    public static final String WEATHER_AVAILABLE = "new-weather-available";
    public static final String WEATHER_FAIL = "weather-job-failed";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        // Get demo weather data.
        CurrentWeather currentWeather = getNewCurrentWeather();
        if (currentWeather != null) {
            WeatherCondition weatherCondition = realm.createObject(WeatherCondition.class);
            weatherCondition.setFetchDate(new Date());

            WeatherHourForecast forecast = new WeatherHourForecast();
            HourForecast hourForecast = new HourForecast();
            hourForecast.weather = currentWeather.weather;
            forecast.addForecast(hourForecast);
            weatherCondition.setForecast(forecast);
            weatherCondition.setLocationLng(0.0);
            weatherCondition.setLocationLat(0.0);

            // We need to commit before the intent or else the results will be empty.
            realm.commitTransaction();

            Intent intent = new Intent(WEATHER_AVAILABLE);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        } else {
            Intent intent = new Intent(WEATHER_FAIL);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }

        // There is a possibility we didn't commit yet.
        if (realm.isInTransaction()) {
            realm.commitTransaction();
        }

        realm.close();

        return Result.SUCCESS;
    }
}