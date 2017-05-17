package com.weatheradviceapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by Casper on 17-5-2017.
 */

public class WeatherVisualizer {

    View v;
    TextView location;
    TextView datetime;
    TextView temp;
    TextView tempUnit;
    ImageView weatherImg;
    TextView sun;
    TextView windSpeed;
    TextView rain;
    TextView cloud;



    public WeatherVisualizer(LayoutInflater inflater, ViewGroup root, WeatherInfo weather) {

        v = inflater.inflate(R.layout.weather_layout, root);

        location = (TextView) v.findViewById(R.id.location);
        datetime = (TextView) v.findViewById(R.id.datetime);
        temp = (TextView) v.findViewById(R.id.temp);
        tempUnit = (TextView) v.findViewById(R.id.tempUnit);
        weatherImg = (ImageView) v.findViewById(R.id.weatherImg);
        sun = (TextView) v.findViewById(R.id.sun);
        windSpeed = (TextView) v.findViewById(R.id.windSpeed);
        rain = (TextView) v.findViewById(R.id.rain);
        cloud = (TextView) v.findViewById(R.id.cloud);

        location.setText(weather.getLocation());

        DateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
        datetime.setText(df.format(Calendar.getInstance().getTime()));
    }
}
