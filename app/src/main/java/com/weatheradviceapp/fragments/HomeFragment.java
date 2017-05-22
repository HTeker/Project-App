package com.weatheradviceapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.WeatherCondition;
import com.weatheradviceapp.views.WeatherVisualizer;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    WeatherVisualizer wvToday1;
    WeatherVisualizer wvToday2;
    WeatherVisualizer wvTomorrow;

    ViewGroup weatherToday1;
    ViewGroup weatherToday2;
    ViewGroup weatherTomorrow;

    private WeatherCondition latestWeatherCondition;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        weatherToday1 = (ViewGroup) view.findViewById(R.id.weatherToday1);

        Calendar cal = Calendar.getInstance();
        wvToday1 = new WeatherVisualizer(getActivity().getLayoutInflater(), weatherToday1, new Weather(), cal.getTime());

        refreshWeatherData();

        return view;
    }

    /**
     * Shows the latest available weather data on screen. Call when new data is retrieved from
     * webservice.
     */
    public void refreshWeatherData() {

        latestWeatherCondition = WeatherCondition.getLatestWeatherCondition();

        if (latestWeatherCondition != null) {
            wvToday1.showWeatherData(latestWeatherCondition.getWeather().weather, latestWeatherCondition.getFetchDate());
        }
    }
}
