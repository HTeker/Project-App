package com.weatheradviceapp.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.WeatherApplication;
import com.weatheradviceapp.helpers.WeatherAdviceGenerator;
import com.weatheradviceapp.helpers.WeatherImageMapper;
import com.weatheradviceapp.models.WeatherCondition;
import com.weatheradviceapp.views.AdviceVisualizer;
import com.weatheradviceapp.views.WeatherVisualizer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private WeatherVisualizer wvToday;
    private List<AdviceVisualizer> adviceVisualizers = new ArrayList<>();

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
        View thisView = inflater.inflate(R.layout.fragment_home, container, false);

        Calendar cal = Calendar.getInstance();
        wvToday = new WeatherVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.weatherToday), new Weather(), cal.getTime());

        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice1)));
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice2)));
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice3)));
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice4)));

        refreshWeatherData(thisView);

        return thisView;
    }

    /**
     * Shows the latest available weather data on screen. Call when new data is retrieved from
     * webservice.
     */
    public void refreshWeatherData() {
        refreshWeatherData(getView().findViewById(R.id.fragment_home));
    }

    public void refreshWeatherData(View view) {

        WeatherCondition latestWeatherCondition = WeatherCondition.getLatestWeatherCondition();

        if (latestWeatherCondition != null) {
            Weather w = latestWeatherCondition.getWeather().weather;
            WeatherImageMapper wim = new WeatherImageMapper(w);

            view.setBackgroundResource(wim.getWeatherBackgroundResource());
            setTextColor((ViewGroup) view, wim.getWeatherForegroundColor(), wim.getWeatherShadowColor());
            wvToday.showWeatherData(w, latestWeatherCondition.getFetchDate());

            // Get all weather conditions for the day planning
            ArrayList<Weather> allWeathers = new ArrayList<>();
            allWeathers.add(latestWeatherCondition.getWeather().weather);
            // TODO: Get forecast for calendar items by location and time

            // Generate advice for all weather conditions
            WeatherAdviceGenerator advGen = new WeatherAdviceGenerator(allWeathers);
            for(int i = 0; i < adviceVisualizers.size(); i++) {
                if (advGen.size() > i && advGen.get(i).getScore() > 40.0f) {
                    adviceVisualizers.get(i).showAdvice(advGen.get(i));
                } else {
                    adviceVisualizers.get(i).clearAdvice();
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
    private void setTextColor(ViewGroup parent, int color, int shadowColor) {
        for (int count=0; count < parent.getChildCount(); count++) {
            View view = parent.getChildAt(count);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                tv.setTextColor(color);
                tv.setShadowLayer(5, 2, 1, shadowColor);
            } else if (view instanceof ImageView) {
                if (view.getId() == R.id.iconCloud ||
                    view.getId() == R.id.iconRain ||
                    view.getId() == R.id.iconSun ||
                    view.getId() == R.id.iconWind) {
                    ImageView imv = (ImageView) view;
                    imv.setImageDrawable(setTint(imv.getDrawable(), color));
                }
            } else if (view instanceof ViewGroup) {
                setTextColor((ViewGroup)view, color, shadowColor);
            }
        }
    }

    public static Drawable setTint(Drawable d, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(d);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }
}
