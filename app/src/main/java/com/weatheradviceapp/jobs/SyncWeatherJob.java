package com.weatheradviceapp.jobs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.survivingwithandroid.weather.lib.model.WeatherHourForecast;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.User;
import com.weatheradviceapp.models.WeatherCondition;

import java.util.Date;

import io.realm.Realm;

public class SyncWeatherJob extends Job {

    public static final String TAG = "sync_weather_job_tag";
    public static final String WEATHER_AVAILABLE = "new-weather-available";
    public static final String WEATHER_FAIL = "weather-job-failed";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        User currentUser = User.getUser();

        // If currentUser is null, the user has not save the settings yet.
        if (currentUser == null) {
            return Result.SUCCESS;
        }

        try {
            WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();
            WeatherConfig config = new WeatherConfig();

            config.ApiKey = getContext().getString(R.string.openweathermap_api_key);

            WeatherClient client = builder.attach(getContext())
                    .provider(new OpenweathermapProviderType())
                    .httpClient(WeatherDefaultClient.class)
                    .config(config)
                    .build();

            // Default location, HRO.
            double lon = 51.917377F;
            double lat = 4.48392F;

            // Get GPS location or location from settings.
            if (!currentUser.isEnabledGPSLocation()) {
                lon = currentUser.getCustomLocationLng();
                lat = currentUser.getCustomLocationLat();
            } else {
                Location bestLocation = getLastBestLocation();
                if (bestLocation == null) {
                    return Result.FAILURE;
                }

                lon = bestLocation.getLongitude();
                lat = bestLocation.getLatitude();
            }

            final double request_lon = lon;
            final double request_lat = lat;

            client.getHourForecastWeather(new WeatherRequest(request_lon, request_lat), new WeatherClient.HourForecastWeatherEventListener() {
                @Override
                public void onWeatherRetrieved(WeatherHourForecast forecast) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    // Inset object into Realm.
                    WeatherCondition weatherCondition = realm.createObject(WeatherCondition.class);
                    weatherCondition.setFetchDate(new Date());
                    weatherCondition.setForecast(forecast);
                    weatherCondition.setLocationLng(request_lon);
                    weatherCondition.setLocationLat(request_lat);
                    realm.commitTransaction();
                    realm.close();

                    Intent intent = new Intent(WEATHER_AVAILABLE);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                }

                @Override
                public void onWeatherError(WeatherLibException e) {
                    Log.d("WL", "Weather Error - parsing data");
                    e.printStackTrace();

                    // Send broadcast to end swipe container animation on fail
                    Intent intent = new Intent(WEATHER_FAIL);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                }

                @Override
                public void onConnectionError(Throwable throwable) {
                    Log.d("WL", "Connection error");
                    throwable.printStackTrace();

                    // Send broadcast to end swipe container animation on fail
                    Intent intent = new Intent(WEATHER_FAIL);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                }
            });
        }
        catch (Throwable t) {
            t.printStackTrace();
            return Result.FAILURE;
        }

        return Result.SUCCESS;
    }

    /**
     * @return the last know best location
     */
    private Location getLastBestLocation() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        Location locationGPS = null;
        Location locationNet = null;

        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(getContext(), perm));
    }
}