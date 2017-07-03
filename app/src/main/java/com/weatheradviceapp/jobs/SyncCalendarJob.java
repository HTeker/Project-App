package com.weatheradviceapp.jobs;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.evernote.android.job.Job;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.okhttp.WeatherDefaultClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.WeatherForecast;
import com.survivingwithandroid.weather.lib.model.WeatherHourForecast;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.User;
import com.weatheradviceapp.models.UserCalendar;
import com.weatheradviceapp.models.UserCalendarEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

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

                // Get the field values
                final long instanceID = cur.getLong(UserCalendar.INSTANCE_PROJECTION_ID_INDEX);
                long eventID = cur.getLong(UserCalendar.INSTANCE_PROJECTION_EVENT_ID_INDEX);
                String title = cur.getString(UserCalendar.INSTANCE_PROJECTION_TITLE_INDEX);
                final long beginVal = cur.getLong(UserCalendar.INSTANCE_PROJECTION_BEGIN_INDEX);
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
                            // Check if we matched addresses, use the first one matched.
                            if (addresses.size() > 0) {

                                // Insert event into Realm.
                                UserCalendarEvent calendar_event = realm.createObject(UserCalendarEvent.class);
                                calendar_event.setInstanceID(instanceID);
                                calendar_event.setEventID(eventID);
                                calendar_event.setTitle(title);
                                calendar_event.setEventBeginDate(new Date(beginVal));
                                calendar_event.setEventEndDate(new Date(endVal));
                                calendar_event.setLocationLng(addresses.get(0).getLongitude());
                                calendar_event.setLocationLat(addresses.get(0).getLatitude());
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

                                SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

                                // Fetch weather data for event.

                                // If event date is today, we can't use forecast weather, we use hourForecast, which also contains more details.
                                if (DATE_FORMAT.format(new Date(beginVal)).equalsIgnoreCase(DATE_FORMAT.format(new Date()))) {
                                    client.getHourForecastWeather(new WeatherRequest(addresses.get(0).getLongitude(), addresses.get(0).getLatitude()), new WeatherClient.HourForecastWeatherEventListener() {
                                        @Override
                                        public void onWeatherRetrieved(WeatherHourForecast forecast) {

                                            // Get the hour difference so we can select the proper weather for this event.
                                            long date_difference = new Date(beginVal).getTime() - new Date().getTime();
                                            int hour = (int)(date_difference / DateUtils.HOUR_IN_MILLIS);

                                            Realm realm = Realm.getDefaultInstance();
                                            realm.beginTransaction();

                                            // Update all events that match this ID.
                                            // We can't do this directly to the realm object because we are in another thread.
                                            RealmResults<UserCalendarEvent> calendar_events = realm.where(UserCalendarEvent.class).equalTo("instanceID", instanceID).findAll();
                                            for (int i=0; i< calendar_events.size(); i++) {
                                                calendar_events.get(i).setWeather(forecast.getHourForecast(hour).weather);
                                            }

                                            realm.commitTransaction();
                                            realm.close();

                                            // Let the app know we have new data.
                                            Intent intent = new Intent(WEATHER_AVAILABLE);
                                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
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
                                } else {
                                    client.getForecastWeather(new WeatherRequest(addresses.get(0).getLongitude(), addresses.get(0).getLatitude()), new WeatherClient.ForecastWeatherEventListener() {
                                        @Override
                                        public void onWeatherRetrieved(WeatherForecast forecast) {

                                            // Get the day difference so we can fetch the weather for the proper day.
                                            long date_difference = new Date(beginVal).getTime() - new Date().getTime();
                                            int day = (int)(date_difference / DateUtils.DAY_IN_MILLIS);

                                            Realm realm = Realm.getDefaultInstance();
                                            realm.beginTransaction();

                                            // Update all events that match this ID.
                                            // We can't do this directly to the realm object because we are in another thread.
                                            RealmResults<UserCalendarEvent> calendar_events = realm.where(UserCalendarEvent.class).equalTo("instanceID", instanceID).findAll();

                                            for (int i=0; i< calendar_events.size(); i++) {

                                                // We need to set some temperature data manually because we don't have the normal data.
                                                // This is actually more detailed.
                                                if (new Date(beginVal).getHours() >= 0 && new Date(beginVal).getHours() < 6) {
                                                    forecast.getForecast(day).weather.temperature.setTemp(forecast.getForecast(day).forecastTemp.night);
                                                }

                                                if (new Date(beginVal).getHours() >= 6 && new Date(beginVal).getHours() < 12) {
                                                    forecast.getForecast(day).weather.temperature.setTemp(forecast.getForecast(day).forecastTemp.morning);
                                                }

                                                if (new Date(beginVal).getHours() >= 12 && new Date(beginVal).getHours() < 18) {
                                                    forecast.getForecast(day).weather.temperature.setTemp(forecast.getForecast(day).forecastTemp.day);
                                                }

                                                if (new Date(beginVal).getHours() >= 18 && new Date(beginVal).getHours() < 24) {
                                                    forecast.getForecast(day).weather.temperature.setTemp(forecast.getForecast(day).forecastTemp.eve);
                                                }

                                                forecast.getForecast(day).weather.temperature.setMinTemp(forecast.getForecast(day).forecastTemp.min);
                                                forecast.getForecast(day).weather.temperature.setMaxTemp(forecast.getForecast(day).forecastTemp.max);

                                                calendar_events.get(i).setWeather(forecast.getForecast(day).weather);
                                            }

                                            realm.commitTransaction();
                                            realm.close();

                                            // Let the app know we have new data.
                                            Intent intent = new Intent(WEATHER_AVAILABLE);
                                            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
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
        realm.close();

        // Let the app know we have new data.
        Intent intent = new Intent(WEATHER_AVAILABLE);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        return Result.SUCCESS;
    }
}