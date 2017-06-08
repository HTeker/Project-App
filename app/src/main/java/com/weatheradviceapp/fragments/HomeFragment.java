package com.weatheradviceapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.helpers.WeatherAdviceGenerator;
import com.weatheradviceapp.models.WeatherCondition;
import com.weatheradviceapp.views.AdviceVisualizer;
import com.weatheradviceapp.views.WeatherVisualizer;

import java.util.ArrayList;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private WeatherVisualizer wvToday1;
    private AdviceVisualizer[] adviceVisualizers = new AdviceVisualizer[4];

    private ViewGroup weatherToday1;

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
        wvToday1 = new WeatherVisualizer(inflater, weatherToday1, new Weather(), cal.getTime());

        adviceVisualizers[0] = new AdviceVisualizer(inflater, (ViewGroup) view.findViewById(R.id.advice1));
        adviceVisualizers[1] = new AdviceVisualizer(inflater, (ViewGroup) view.findViewById(R.id.advice2));
        adviceVisualizers[2] = new AdviceVisualizer(inflater, (ViewGroup) view.findViewById(R.id.advice3));
        adviceVisualizers[3] = new AdviceVisualizer(inflater, (ViewGroup) view.findViewById(R.id.advice4));


        refreshWeatherData();

        return view;
    }

    /**
     * Shows the latest available weather data on screen. Call when new data is retrieved from
     * webservice.
     */
    public void refreshWeatherData() {

        WeatherCondition latestWeatherCondition = WeatherCondition.getLatestWeatherCondition();

        if (latestWeatherCondition != null) {
            wvToday1.showWeatherData(latestWeatherCondition.getWeather().weather, latestWeatherCondition.getFetchDate());


            // Get all weather conditions for the day planning
            ArrayList<Weather> allWeathers = new ArrayList<>();
            allWeathers.add(latestWeatherCondition.getWeather().weather);
            // TODO: Get forecast for calendar items by location and time

            // Generate advice for all weather conditions
            WeatherAdviceGenerator advGen = new WeatherAdviceGenerator(allWeathers);

            for(int i = 0; i < adviceVisualizers.length; i++) {
                if (advGen.getAdviceList().size() > i) { // && advGen.getAdviceList().get(i).getScore() > 40.0f) {
                    adviceVisualizers[i].showAdvice(advGen.getAdviceList().get(i));
                } else {
                    adviceVisualizers[i].clearAdvice();
                }
            }
        }
    }
}
