package com.weatheradviceapp.jobs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.evernote.android.job.Job;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.HourForecast;
import com.survivingwithandroid.weather.lib.model.WeatherHourForecast;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProvider;
import com.weatheradviceapp.models.UserCalendarEvent;
import com.weatheradviceapp.models.WeatherCondition;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class DemoCalendarJob extends Job {

    public static final String TAG = "demo_calendar_job_tag";
    public static final String WEATHER_AVAILABLE = "new-calendar-available";
    public static final String WEATHER_FAIL = "calendar-job-failed";

    // For weather condition codes from OpenWeatherMap see: http://www.openweathermap.org/weather-conditions
    private static final String[] DEMO_DATA = {
            "{\"coord\":{\"lon\":4.4203586,\"lat\":51.9280573},\"weather\":[{\"id\":300,\"main\":\"Drizzle\",\"description\":\"light intensity drizzle\",\"icon\":\"09d\"}],\"base\":\"stations\",\"main\":{\"temp\":14.32,\"pressure\":1012,\"humidity\":81,\"temp_min\":12.15,\"temp_max\":15.15},\"visibility\":10000,\"wind\":{\"speed\":4.1,\"deg\":80},\"clouds\":{\"all\":90},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"NL\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2747891,\"name\":\"Rotterdam\",\"cod\":200}",
            "{\"coord\":{\"lon\":4.4203586,\"lat\":51.9280573},\"weather\":[{\"id\":803,\"main\":\"Clouds\",\"description\":\"broken clouds\",\"icon\":\"04d\"}],\"base\":\"stations\",\"main\":{\"temp\":15.0,\"pressure\":1019,\"humidity\":83,\"temp_min\":19.12,\"temp_max\":21.37},\"visibility\":10000,\"wind\":{\"speed\":25.1,\"deg\":240},\"clouds\":{\"all\":75},\"rain\":{\"3h\":3},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"NL\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2747891,\"name\":\"Rotterdam\",\"cod\":200}",
            "{\"coord\":{\"lon\":4.4203586,\"lat\":51.9280573},\"weather\":[{\"id\":600,\"main\":\"Snow\",\"description\":\"light snow\",\"icon\":\"13d\"}],\"base\":\"stations\",\"main\":{\"temp\":0.0,\"pressure\":1019,\"humidity\":83,\"temp_min\":-4.12,\"temp_max\":1.37},\"visibility\":10000,\"wind\":{\"speed\":5.1,\"deg\":150},\"clouds\":{\"all\":45},\"rain\":{\"3h\":0},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"NL\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2747891,\"name\":\"Rotterdam\",\"cod\":200}",
            "{\"coord\":{\"lon\":4.4203586,\"lat\":51.9280573},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"base\":\"stations\",\"main\":{\"temp\":24.0,\"pressure\":1031,\"humidity\":63,\"temp_min\":22.12,\"temp_max\":26.37},\"visibility\":10000,\"wind\":{\"speed\":5.1,\"deg\":60},\"clouds\":{\"all\":45},\"rain\":{\"3h\":0},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"NL\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2747891,\"name\":\"Rotterdam\",\"cod\":200}",
            "{\"coord\":{\"lon\":4.4203586,\"lat\":51.9280573},\"weather\":[{\"id\":212,\"main\":\"Thunderstorm\",\"description\":\"thunderstorm with heavy rain\",\"icon\":\"11d\"}],\"base\":\"stations\",\"main\":{\"temp\":17.0,\"pressure\":1001,\"humidity\":100,\"temp_min\":14.12,\"temp_max\":18.37},\"visibility\":10000,\"wind\":{\"speed\":35.1,\"deg\":85},\"clouds\":{\"all\":97},\"rain\":{\"3h\":45},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"NL\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2747891,\"name\":\"Rotterdam\",\"cod\":200}",
            "{\"coord\":{\"lon\":4.4203586,\"lat\":51.9280573},\"weather\":[{\"id\":741,\"main\":\"Fog\",\"description\":\"fog\",\"icon\":\"50d\"}],\"base\":\"stations\",\"main\":{\"temp\":12.0,\"pressure\":1001,\"humidity\":100,\"temp_min\":8.92,\"temp_max\":13.37},\"visibility\":10000,\"wind\":{\"speed\":15.1,\"deg\":150},\"clouds\":{\"all\":99},\"rain\":{\"3h\":0.2},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"NL\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2747891,\"name\":\"Rotterdam\",\"cod\":200}",
            "{\"coord\":{\"lon\":4.4203586,\"lat\":51.9280573},\"weather\":[{\"id\":801,\"main\":\"Clouds\",\"description\":\"few clouds \",\"icon\":\"02d\"}],\"base\":\"stations\",\"main\":{\"temp\":24.0,\"pressure\":1031,\"humidity\":78,\"temp_min\":22.12,\"temp_max\":26.37},\"visibility\":10000,\"wind\":{\"speed\":18.6,\"deg\":50},\"clouds\":{\"all\":35},\"rain\":{\"3h\":0},\"dt\":1485789600,\"sys\":{\"type\":1,\"id\":5091,\"message\":0.0103,\"country\":\"NL\",\"sunrise\":1485762037,\"sunset\":1485794875},\"id\":2747891,\"name\":\"Rotterdam\",\"cod\":200}",
    };

    private static int demoWeatherIndex = 0;

    @Override
    @NonNull
    protected Result onRunJob(Params params) {

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        // Clear all event records for clean demo.
        RealmResults<UserCalendarEvent> events = realm.where(UserCalendarEvent.class).findAllSorted("eventBeginDate", Sort.ASCENDING);
        for (int i = 0; i < events.size(); i++) {
            realm.beginTransaction();
            events.get(i).deleteFromRealm();
            realm.commitTransaction();
        }

        CurrentWeather currentWeather = getNewCurrentWeather();
        if (currentWeather != null) {

            realm.beginTransaction();
            UserCalendarEvent userCalendarEvent1 = realm.createObject(UserCalendarEvent.class);

            Date dt = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            c.add(Calendar.DATE, 1);
            dt = c.getTime();

            userCalendarEvent1.setEventBeginDate(dt);
            userCalendarEvent1.setTitle("App presenteren");
            userCalendarEvent1.setLocationTitle("Hogeschool Rotterdam");
            userCalendarEvent1.setWeather(currentWeather.weather);
            userCalendarEvent1.setLocationLng(0.0);
            userCalendarEvent1.setLocationLat(0.0);
            realm.commitTransaction();

            CurrentWeather secondCurrentWeather = getNewCurrentWeather();
            if (secondCurrentWeather != null) {

                realm.beginTransaction();
                UserCalendarEvent userCalendarEvent2 = realm.createObject(UserCalendarEvent.class);

                c.add(Calendar.DATE, 2);
                dt = c.getTime();

                userCalendarEvent2.setEventBeginDate(dt);
                userCalendarEvent2.setTitle("Zomervakantie vieren");
                userCalendarEvent2.setLocationTitle("Club Hollywood Rotterdam");
                userCalendarEvent2.setWeather(secondCurrentWeather.weather);
                userCalendarEvent2.setLocationLng(0.0);
                userCalendarEvent2.setLocationLat(0.0);

                realm.commitTransaction();
            }

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
            result = wp.getCurrentCondition(DEMO_DATA[demoWeatherIndex]);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }

        demoWeatherIndex++;
        if (DEMO_DATA.length <= demoWeatherIndex) {
            demoWeatherIndex = 0;
        }

        return result;
    }
}