package com.weatheradviceapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.WeatherApplication;
import com.weatheradviceapp.helpers.WeatherAdviceGenerator;
import com.weatheradviceapp.helpers.WeatherImageMapper;
import com.weatheradviceapp.models.Advice;
import com.weatheradviceapp.models.WeatherCondition;
import com.weatheradviceapp.views.AdviceVisualizer;
import com.weatheradviceapp.views.WeatherVisualizer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private View thisView;
    private WeatherVisualizer wvToday;
    private AdviceVisualizer[] adviceVisualizers = new AdviceVisualizer[4];

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
        thisView = inflater.inflate(R.layout.fragment_home, container, false);

        Calendar cal = Calendar.getInstance();
        wvToday = new WeatherVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.weatherToday), new Weather(), cal.getTime());

        adviceVisualizers[0] = new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice1));
        adviceVisualizers[1] = new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice2));
        adviceVisualizers[2] = new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice3));
        adviceVisualizers[3] = new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice4));


        refreshWeatherData();

        return thisView;
    }

    /**
     * Shows the latest available weather data on screen. Call when new data is retrieved from
     * webservice.
     */
    public void refreshWeatherData() {

        WeatherCondition latestWeatherCondition = WeatherCondition.getLatestWeatherCondition();

        if (latestWeatherCondition != null) {
            Weather w = latestWeatherCondition.getWeather().weather;
            Weather.Condition c = w.currentCondition;
            thisView.setBackgroundResource(WeatherImageMapper.getWeatherBackgroundResource(c.getIcon(), c.getWeatherId()));
            int color = ContextCompat.getColor(WeatherApplication.getContext(), WeatherImageMapper.getWeatherForegroundColor(c.getIcon(), c.getWeatherId()));
            setTextColor((ViewGroup) thisView, color);

            wvToday.showWeatherData(w, latestWeatherCondition.getFetchDate());


            // Get all weather conditions for the day planning
            ArrayList<Weather> allWeathers = new ArrayList<>();
            allWeathers.add(latestWeatherCondition.getWeather().weather);
            // TODO: Get forecast for calendar items by location and time

            // Generate advice for all weather conditions
            WeatherAdviceGenerator advGen = new WeatherAdviceGenerator(allWeathers);

            for(int i = 0; i < adviceVisualizers.length; i++) {
                if (advGen.size() > i && advGen.get(i).getScore() > 40.0f) {
                    adviceVisualizers[i].showAdvice(advGen.get(i));
                } else {
                    adviceVisualizers[i].clearAdvice();
                }
            }
        }
    }

    /**
     * Recursively loop through all views to find TextViews and change the text color.
     *
     * @param parent
     * @param color
     */
    private void setTextColor(ViewGroup parent, int color) {
        for (int count=0; count < parent.getChildCount(); count++){
            View view = parent.getChildAt(count);
            if(view instanceof TextView){
                ((TextView)view).setTextColor(color);
            } else if(view instanceof ViewGroup){
                setTextColor((ViewGroup)view, color);
            }
        }
    }
}
