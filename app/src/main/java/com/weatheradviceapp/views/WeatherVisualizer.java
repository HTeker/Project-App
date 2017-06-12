package com.weatheradviceapp.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.helpers.WeatherImageMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherVisualizer {

    private View wv;
    private TextView location;
    private TextView datetime;
    private TextView temp;
    private TextView sun;
    private TextView windSpeed;
    private TextView rain;
    private TextView cloud;

    private ImageView weatherImg;

    private ViewGroup container;


    public WeatherVisualizer(LayoutInflater inflater, ViewGroup container, Weather weather, Date date) {

        // Initialize view
        wv = inflater.inflate(R.layout.weather_layout, container);

        location = (TextView) wv.findViewById(R.id.location);
        datetime = (TextView) wv.findViewById(R.id.datetime);
        temp = (TextView) wv.findViewById(R.id.temp);
        sun = (TextView) wv.findViewById(R.id.sun);
        windSpeed = (TextView) wv.findViewById(R.id.windSpeed);
        rain = (TextView) wv.findViewById(R.id.rain);
        cloud = (TextView) wv.findViewById(R.id.cloud);
        weatherImg = (ImageView) wv.findViewById(R.id.weatherImg);

        this.container = container;



        showWeatherData(weather, date);
    }

    public void showWeatherData(Weather weather, Date date) {

        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
        datetime.setText(df.format(date));

        TextView calendar_date = (TextView) wv.findViewById(R.id.calendar_date);

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("H:m");
        calendar_date.setText(DATE_FORMAT.format(date));

        if (null != weather.location.getCity()) {

            // Set values in views
            location.setText(weather.location.getCity());

            weatherImg.setImageResource(new WeatherImageMapper(weather).getWeatherIconResource());
            temp.setText(String.format(java.util.Locale.getDefault(), "%.0f", weather.temperature.getTemp()));
            sun.setText(String.format(java.util.Locale.getDefault(), "%.0f", weather.currentCondition.getUV()));
            windSpeed.setText(String.format(java.util.Locale.getDefault(), "%.0f", weather.wind.getSpeed() * 3.6f) + " " + container.getContext().getString(R.string.wind_speed_unit_kph));
            cloud.setText(String.format(java.util.Locale.getDefault(), "%d %%", weather.clouds.getPerc()));

            // TODO: Get forecast for rain prediction
            // The rain on the currentWeather is only representing the volume in mm from the last 3 hours.
            if (weather.rain.length > 0) {
                rain.setText(String.format(java.util.Locale.getDefault(), "%.0f %%", weather.rain[0].getChance()));
            } else {
                rain.setText("-");
            }
        }
    }

    public void show() {
        wv.setVisibility(View.VISIBLE);
    }

    public void hide() {
        wv.setVisibility(View.GONE);
    }

    public void setText(String text) {
        location.setText(text);
    }

    public void setLocation(String text) {
        RelativeLayout weatherLayout = (RelativeLayout) wv.findViewById(R.id.weatherInfo);
        RelativeLayout locationLayout = (RelativeLayout) wv.findViewById(R.id.locationInfo);
        weatherLayout.setVisibility(View.GONE);
        locationLayout.setVisibility(View.VISIBLE);

        TextView calendar_location = (TextView) wv.findViewById(R.id.calendar_location);
        calendar_location.setText(text);
    }
}
