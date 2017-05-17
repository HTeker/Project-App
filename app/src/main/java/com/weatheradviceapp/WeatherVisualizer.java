package com.weatheradviceapp;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.model.Weather;

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


    public WeatherVisualizer(LayoutInflater inflater, ViewGroup root, Weather weather, Date date) {

        this.weather = weather;
        this.date = date;

        wv = inflater.inflate(R.layout.weather_layout, root);

        location = (TextView) wv.findViewById(R.id.location);
        datetime = (TextView) wv.findViewById(R.id.datetime);
        temp = (TextView) wv.findViewById(R.id.temp);
        tempUnit = (TextView) wv.findViewById(R.id.tempUnit);
        sun = (TextView) wv.findViewById(R.id.sun);
        windSpeed = (TextView) wv.findViewById(R.id.windSpeed);
        rain = (TextView) wv.findViewById(R.id.rain);
        cloud = (TextView) wv.findViewById(R.id.cloud);
        weatherImg = (ImageView) wv.findViewById(R.id.weatherImg);

        if (null != weather.location.getCity()) {

            // Zet nu de waarden in de textviews
            location.setText(weather.location.getClass().toString());
            DateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
            datetime.setText(df.format(date));
            temp.setText(Math.round(weather.temperature.getTemp()));
            sun.setText(Math.round(weather.currentCondition.getUV()));
            windSpeed.setText(Math.round(weather.wind.getSpeed()) + Resources.getSystem().getString(R.string.windspeed_unit_kph));

            // In de weather class worden 2 rain classes geinstantieerd. geen docs kunnen vinden over inhoud.
            rain.setText(Math.round(weather.rain[0].getChance()) + " %");
        }
    }
}
