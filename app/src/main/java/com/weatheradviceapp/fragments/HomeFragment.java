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
import com.weatheradviceapp.views.WeatherVisualizer;

import java.util.ArrayList;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private WeatherVisualizer wvToday1;

    private ViewGroup weatherToday1;

    private TextView[] tv = new TextView[3];

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

        tv[0] = (TextView) view.findViewById(R.id.textView2);
        tv[1] = (TextView) view.findViewById(R.id.textView3);
        tv[2] = (TextView) view.findViewById(R.id.textView4);


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

        WeatherCondition latestWeatherCondition = WeatherCondition.getLatestWeatherCondition();

        if (latestWeatherCondition != null) {
            wvToday1.showWeatherData(latestWeatherCondition.getWeather().weather, latestWeatherCondition.getFetchDate());


            // Generate advice for all weather conditions
            //ArrayList<Weather> allWeathers = new ArrayList<>();
            //allWeathers.add(latestWeatherCondition.getWeather().weather);
            //WeatherAdviceGenerator advGen = new WeatherAdviceGenerator(allWeathers);

            //for(int i = 0; i < tv.length; i++) {
            //    tv[i].setText(getString(advGen.getAdviceList().get(i).getAdviceStringResource()));
            //}
        }
    }
}
