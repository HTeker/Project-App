package com.weatheradviceapp.jobs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.evernote.android.job.Job;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.WeatherForecast;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.User;
import com.weatheradviceapp.models.UserCalendar;
import com.weatheradviceapp.models.UserCalendarEvent;
import com.weatheradviceapp.models.WeatherCondition;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmList;

public class SyncCalendarJob extends Job {

    public static final String TAG = "sync_calendar_job_tag";
    public static final String WEATHER_AVAILABLE = "new-calendar-available";
    public static final String WEATHER_FAIL = "calendar-job-failed";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        User currentUser = User.getUser();

        // If currentUser is null, the user has not save the settings yet.
        if (currentUser == null) {
            return Result.SUCCESS;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmList<UserCalendar> user_enabled_agendas = currentUser.getAgendas();

        // Loop through all enabled agenda's.
        for (int i=0; i< user_enabled_agendas.size(); i++) {
            UserCalendar calendar = user_enabled_agendas.get(i);
            Cursor cur = calendar.getInstanceCursor();
            RealmList<UserCalendarEvent> events = new RealmList();
            // Loop through all found instances.
            while (cur.moveToNext()) {

                UserCalendarEvent calendar_event = realm.createObject(UserCalendarEvent.class);

                // Get the field values
                long instanceID = cur.getLong(UserCalendar.INSTANCE_PROJECTION_ID_INDEX);
                long eventID = cur.getLong(UserCalendar.INSTANCE_PROJECTION_EVENT_ID_INDEX);
                String title = cur.getString(UserCalendar.INSTANCE_PROJECTION_TITLE_INDEX);
                long beginVal = cur.getLong(UserCalendar.INSTANCE_PROJECTION_BEGIN_INDEX);
                long endVal = cur.getLong(UserCalendar.INSTANCE_PROJECTION_END_INDEX);

                // Lookup the event of this instance.
                Cursor event = UserCalendar.getEvent(eventID);
                if (event.getCount() > 0) {
                    event.moveToFirst();
                    String location = event.getString(UserCalendar.EVENT_PROJECTION_EVENT_LOCATION_INDEX);
                    if (!location.equalsIgnoreCase("")) {
                        Geocoder geocoder = new Geocoder(this.getContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocationName(location, 1);
                            if (addresses.size() > 0) {
                                calendar_event.setInstanceID(instanceID);
                                calendar_event.setEventID(eventID);
                                calendar_event.setTitle(title);
                                calendar_event.setEventBeginDate(new Date(beginVal));
                                calendar_event.setEventEndDate(new Date(endVal));
                                calendar_event.setLocationLat(addresses.get(0).getLatitude());
                                calendar_event.setLocationLng(addresses.get(0).getLongitude());
                                calendar_event.setLocationTitle(location);
                                events.add(calendar_event);

                                WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();
                                WeatherConfig config = new WeatherConfig();

                                config.ApiKey = getContext().getString(R.string.openweathermap_api_key);

                                WeatherClient client = builder.attach(getContext())
                                        .provider(new OpenweathermapProviderType())
                                        .httpClient(WeatherDefaultClient.class)
                                        .config(config)
                                        .build();

                                client.getForecastWeather(new WeatherRequest(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()), new WeatherClient.ForecastWeatherEventListener() {
                                    @Override
                                    public void onWeatherRetrieved(WeatherForecast forecast) {

                                    }

                                    @Override
                                    public void onWeatherError(WeatherLibException e) {
                                        Log.d("WL", "Weather Error - parsing data");
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onConnectionError(Throwable throwable) {
                                        Log.d("WL", "Connection error");
                                        throwable.printStackTrace();
                                    }
                                });

                            }
                        }
                        catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }
            }

            calendar.setEvents(events);
        }

        realm.commitTransaction();

        Intent intent = new Intent(WEATHER_AVAILABLE);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        return Result.SUCCESS;
    }
}