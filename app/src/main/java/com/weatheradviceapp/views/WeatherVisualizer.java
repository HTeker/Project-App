package com.weatheradviceapp.views;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Casper on 17-5-2017.
 */

public class WeatherVisualizer {

    Weather weather;
    Date date;
    View wv;
    TextView location;
    TextView datetime;
    TextView temp;
    TextView tempUnit;
    TextView sun;
    TextView windSpeed;
    TextView rain;
    TextView cloud;

    ImageView weatherImg;

    ViewGroup container;


    public WeatherVisualizer(LayoutInflater inflater, ViewGroup container, Weather weather, Date date) {

        // Initialize view
        wv = inflater.inflate(R.layout.weather_layout, container);

        location = (TextView) wv.findViewById(R.id.location);
        datetime = (TextView) wv.findViewById(R.id.datetime);
        temp = (TextView) wv.findViewById(R.id.temp);
        tempUnit = (TextView) wv.findViewById(R.id.tempUnit);
        sun = (TextView) wv.findViewById(R.id.sun);
        windSpeed = (TextView) wv.findViewById(R.id.windSpeed);
        rain = (TextView) wv.findViewById(R.id.rain);
        cloud = (TextView) wv.findViewById(R.id.cloud);
        weatherImg = (ImageView) wv.findViewById(R.id.weatherImg);

        this.container = container;

        showWeatherData(weather, date);
    }

    public void showWeatherData(Weather weather, Date date) {

        this.weather = weather;
        this.date = date;

        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
        datetime.setText(df.format(date));

        if (null != weather.location.getCity()) {

            // Set values in textviews
            location.setText(weather.location.getCity());

            temp.setText(String.format(java.util.Locale.getDefault(), "%.0f", weather.temperature.getTemp()));
            sun.setText(String.format(java.util.Locale.getDefault(), "%.0f", weather.currentCondition.getUV()));

            windSpeed.setText(String.format(java.util.Locale.getDefault(), "%.0f", weather.wind.getSpeed()) + " " + container.getContext().getString(R.string.windspeed_unit_kph));

            // Weather index 0 = in 1 h.
            // Weather index 1 = in 3 h.
            if (weather.rain.length > 0) {
                rain.setText(String.format(java.util.Locale.getDefault(), "%.0f %%", weather.rain[0].getChance()));
            }

            cloud.setText(String.format(java.util.Locale.getDefault(), "%d %%", weather.clouds.getPerc()));
        }
    }
}
