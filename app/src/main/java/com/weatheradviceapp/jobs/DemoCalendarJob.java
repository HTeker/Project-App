package com.weatheradviceapp.jobs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.UserCalendarEvent;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class DemoCalendarJob extends DemoJob {

    public static final String TAG = "demo_calendar_job_tag";
    public static final String WEATHER_AVAILABLE = "new-calendar-available";
    public static final String WEATHER_FAIL = "calendar-job-failed";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        Realm realm = Realm.getDefaultInstance();

        // Clear all event records for clean demo.
        final RealmResults<UserCalendarEvent> results = realm.where(UserCalendarEvent.class).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                results.deleteAllFromRealm();
            }
        });

        realm.beginTransaction();

        // Get demo weather data.
        CurrentWeather currentWeather = getNewCurrentWeather();
        if (currentWeather != null) {
            UserCalendarEvent userCalendarEvent1 = realm.createObject(UserCalendarEvent.class);

            Date dt = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            c.add(Calendar.DATE, 1);
            dt = c.getTime();

            userCalendarEvent1.setEventBeginDate(dt);
            userCalendarEvent1.setTitle(getContext().getString(R.string.demo_demonstrate_app));
            userCalendarEvent1.setLocationTitle(getContext().getString(R.string.demo_university_rotterdam));
            userCalendarEvent1.setWeather(currentWeather.weather);
            userCalendarEvent1.setLocationLng(0.0);
            userCalendarEvent1.setLocationLat(0.0);

            CurrentWeather secondCurrentWeather = getNewCurrentWeather();
            if (secondCurrentWeather != null) {
                UserCalendarEvent userCalendarEvent2 = realm.createObject(UserCalendarEvent.class);

                c.add(Calendar.DATE, 2);
                dt = c.getTime();

                userCalendarEvent2.setEventBeginDate(dt);
                userCalendarEvent2.setTitle(getContext().getString(R.string.demo_celebrate_summer));
                userCalendarEvent2.setLocationTitle(getContext().getString(R.string.demo_club_hollywood_rotterdam));
                userCalendarEvent2.setWeather(secondCurrentWeather.weather);
                userCalendarEvent2.setLocationLng(0.0);
                userCalendarEvent2.setLocationLat(0.0);
            }

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