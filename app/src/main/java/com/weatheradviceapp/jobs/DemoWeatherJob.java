package com.weatheradviceapp.jobs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.evernote.android.job.Job;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProvider;
import com.weatheradviceapp.models.User;
import com.weatheradviceapp.models.WeatherCondition;

import java.util.Date;

import io.realm.Realm;

public class DemoWeatherJob extends Job {

    public static final String TAG = "demo_weather_job_tag";
    public static final String WEATHER_AVAILABLE = "new-weather-available";
    public static final String WEATHER_FAIL = "weather-job-failed";

    private static int demoWeatherIndex = 0;

    public static final String[] demoData = {
            "{\"coord\":{\"lon\":4.4203586,\"lat\":51.9280573},\"weather\":[{\"id\":300,\"main\":\"Drizzle\",\"description\":\"light intensity drizzle\",\"icon\":\"09d\"}],\"base\":\"stations\",\"main\":{\"temp\":14.32,\"pressure\":1012,\"humidity\":81,\"temp_min\":12.15,\"temp_max\":15.15},\"visibility\":10000,\"wind\":{\"speed\":4.1,\"deg\":80},\"clouds\":{\"all\":90},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"NL\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2747891,\"name\":\"Rotterdam\",\"cod\":200}",
            "{\"coord\":{\"lon\":4.4203586,\"lat\":51.9280573},\"weather\":[{\"id\":803,\"main\":\"Clouds\",\"description\":\"broken clouds\",\"icon\":\"04n\"}],\"base\":\"stations\",\"main\":{\"temp\":19.85,\"pressure\":1019,\"humidity\":83,\"temp_min\":19.12,\"temp_max\":21.37},\"visibility\":10000,\"wind\":{\"speed\":5.1,\"deg\":150},\"clouds\":{\"all\":75},\"rain\":{\"3h\":3},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"NL\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2747891,\"name\":\"Rotterdam\",\"cod\":200}"
    };

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        User currentUser = User.getUser();

        // If currentUser is null, the user has not save the settings yet.
        if (currentUser == null) {
            return Result.SUCCESS;
        }

        CurrentWeather currentWeather = getNewCurrentWeather();

        if (currentWeather != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            WeatherCondition weatherCondition = realm.createObject(WeatherCondition.class);
            weatherCondition.setFetchDate(new Date());
            weatherCondition.setWeather(currentWeather);
            weatherCondition.setLocationLng(0.0);
            weatherCondition.setLocationLat(0.0);
            realm.commitTransaction();

            Intent intent = new Intent(WEATHER_AVAILABLE);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        } else {
            Intent intent = new Intent(WEATHER_FAIL);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }
        return Result.SUCCESS;
    }

    private CurrentWeather getNewCurrentWeather() {
        // Let's create the WeatherProvider
        WeatherConfig config = new WeatherConfig();
        config.unitSystem = WeatherConfig.UNIT_SYSTEM.M;
        OpenweathermapProvider wp = new OpenweathermapProvider();
        wp.setConfig(config);

        CurrentWeather result = null;
        try {
            result = wp.getCurrentCondition(demoData[demoWeatherIndex]);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }

        demoWeatherIndex++;
        if (demoData.length <= demoWeatherIndex) {
            demoWeatherIndex = 0;
        }

        return result;
    }
}