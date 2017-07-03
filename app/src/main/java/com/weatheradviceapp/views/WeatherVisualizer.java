package com.weatheradviceapp.views;

import android.transition.AutoTransition;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.fragments.HomeFragment;
import com.weatheradviceapp.helpers.AdviceFactory;
import com.weatheradviceapp.helpers.WeatherAdviceGenerator;
import com.weatheradviceapp.helpers.WeatherImageMapper;
import com.weatheradviceapp.models.UserCalendarEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherVisualizer {

    private View wv;
    private TextView location;
    private TextView datetime;
    private TextView temp;
    private TextView humidity;
    private TextView windSpeed;
    private TextView rain;
    private TextView cloud;
    private TextView calendar_location;

    private ImageView weatherImg;
    private ImageView windDirection;

    private ViewGroup container;

    private List<AdviceVisualizer> adviceVisualizers;

    private Scene closedScene;
    private Scene openScene;
    private ViewGroup mSceneRoot;

    private boolean adviceDetails = false;

    public WeatherVisualizer(LayoutInflater inflater, ViewGroup container, Weather weather, Date date) {

        this.container = container;

        final LayoutInflater myInflater = inflater;

        // Initialize view.
        wv = inflater.inflate(R.layout.weather_layout, container);

        mSceneRoot = (ViewGroup) wv.findViewById(R.id.advices_scene);

        // Toggle advice display when advice icons/text are clicked.
        mSceneRoot.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                toggleAdviceDetail(view);
            }
        });

        closedScene = Scene.getSceneForLayout(mSceneRoot, R.layout.weather_layout_scene_closed, container.getContext());
        // Update views when we transitioned.
        closedScene.setEnterAction(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < adviceVisualizers.size(); i++) {
                    if (i == 0) {
                        adviceVisualizers.get(i).findNewViews(myInflater, (ViewGroup)wv.findViewById(R.id.advice1));
                    }
                    if (i == 1) {
                        adviceVisualizers.get(i).findNewViews(myInflater, (ViewGroup)wv.findViewById(R.id.advice2));
                    }
                    if (i == 2) {
                        adviceVisualizers.get(i).findNewViews(myInflater, (ViewGroup)wv.findViewById(R.id.advice3));
                    }
                    if (i == 3) {
                        adviceVisualizers.get(i).findNewViews(myInflater, (ViewGroup)wv.findViewById(R.id.advice4));
                    }
                    adviceVisualizers.get(i).hideText();

                    HomeFragment.setTextColor(mSceneRoot, location.getCurrentTextColor(), location.getShadowColor());
                }
            }
        });

        openScene = Scene.getSceneForLayout(mSceneRoot, R.layout.weather_layout_scene_open, container.getContext());
        // Update views when we transitioned.
        openScene.setEnterAction(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < adviceVisualizers.size(); i++) {
                    if (i == 0) {
                        adviceVisualizers.get(i).findNewViews(myInflater, (ViewGroup)wv.findViewById(R.id.advice1));
                    }
                    if (i == 1) {
                        adviceVisualizers.get(i).findNewViews(myInflater, (ViewGroup)wv.findViewById(R.id.advice2));
                    }
                    if (i == 2) {
                        adviceVisualizers.get(i).findNewViews(myInflater, (ViewGroup)wv.findViewById(R.id.advice3));
                    }
                    if (i == 3) {
                        adviceVisualizers.get(i).findNewViews(myInflater, (ViewGroup)wv.findViewById(R.id.advice4));
                    }
                    adviceVisualizers.get(i).showText();
                }

                HomeFragment.setTextColor(mSceneRoot, location.getCurrentTextColor(), location.getShadowColor());
            }
        });

        location = (TextView) wv.findViewById(R.id.location);
        datetime = (TextView) wv.findViewById(R.id.datetime);
        temp = (TextView) wv.findViewById(R.id.temp);
        humidity = (TextView) wv.findViewById(R.id.humidity);
        windSpeed = (TextView) wv.findViewById(R.id.windSpeed);
        windDirection = (ImageView) wv.findViewById(R.id.windDirection);
        rain = (TextView) wv.findViewById(R.id.rain);
        cloud = (TextView) wv.findViewById(R.id.cloud);
        weatherImg = (ImageView) wv.findViewById(R.id.weatherImg);
        calendar_location = (TextView) wv.findViewById(R.id.calendar_location);

        adviceVisualizers = new ArrayList<>();
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) wv.findViewById(R.id.advice1)));
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) wv.findViewById(R.id.advice2)));
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) wv.findViewById(R.id.advice3)));
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) wv.findViewById(R.id.advice4)));

        showWeatherData(weather, date, null);
    }

    public void showWeatherData(Weather weather, Date date, UserCalendarEvent calendarEvent) {
        AdviceFactory.Filter adviceFilter = AdviceFactory.Filter.ALL;
        if (calendarEvent != null) {
            adviceFilter = AdviceFactory.Filter.CLOTHING;
        }
        // Get all weather conditions for the day planning
        ArrayList<Weather> allWeathers = new ArrayList<>();
        allWeathers.add(weather);

        // Generate advice for all weather conditions
        WeatherAdviceGenerator advGen = new WeatherAdviceGenerator(allWeathers, adviceFilter);
        boolean showedAnyAdvices = false;
        for(int i = 0; i < adviceVisualizers.size(); i++) {

            // Only show advice when score is high enough.
            if (advGen.size() > i && advGen.get(i).getScore() > 40.0f) {
                adviceVisualizers.get(i).showAdvice(advGen.get(i));
                adviceVisualizers.get(i).hideText();
                showedAnyAdvices = true;
            } else {
                adviceVisualizers.get(i).clearAdvice();
            }
        }

        // Hide advices when we didn't show any.
        if (!showedAnyAdvices) {
            mSceneRoot.setVisibility(View.GONE);
        }

        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
        datetime.setText(df.format(date));

        TextView calendar_date = (TextView) wv.findViewById(R.id.datetime);

        String date_format = "HH:mm";

        if (calendarEvent != null) {
            date_format = "dd-MM-yyyy HH:mm";
        }

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(date_format);
        calendar_date.setText(DATE_FORMAT.format(date));

        if (null != weather.location.getCity()) {

            // Set values in views.
            location.setText(weather.location.getCity());

            if (calendarEvent != null) {

                // Calendar event shows different data than normal weather.
                location.setText(weather.location.getCity());
                calendar_location.setText(calendarEvent.getTitle());
                calendar_location.setVisibility(View.VISIBLE);
            } else {
                calendar_location.setVisibility(View.GONE);
            }

            weatherImg.setImageResource(new WeatherImageMapper(weather).getWeatherIconResource());
            temp.setText(String.format(java.util.Locale.getDefault(), "%.0f", weather.temperature.getTemp()));
            humidity.setText(String.format(java.util.Locale.getDefault(), "%.0f %%", weather.currentCondition.getHumidity()));
            windSpeed.setText(String.format(java.util.Locale.getDefault(), "%.0f", weather.wind.getSpeed() * 3.6f) + " " + container.getContext().getString(R.string.wind_speed_unit_kph));
            windDirection.setRotation(weather.wind.getDeg());
            cloud.setText(String.format(java.util.Locale.getDefault(), "%d %%", weather.clouds.getPerc()));

            // Make sure we have rain data, WeatherLib automatically fills the rain, but the actual data is empty.
            if (weather.rain.length > 0 && (weather.rain[0].getTime() != null || weather.rain[1].getTime() != null)) {
                if (weather.rain[0].getTime() != null) {
                    rain.setText(String.format(java.util.Locale.getDefault(), "%.0f %%", weather.rain[0].getChance()));
                }
                else if (weather.rain[1].getTime() != null) {
                    rain.setText(String.format(java.util.Locale.getDefault(), "%.0f %%", weather.rain[1].getChance()));
                }
            } else {
                rain.setText(String.format(java.util.Locale.getDefault(), "%.0f %%", 0.0f));
            }
        }
    }

    public void show() {
        wv.setVisibility(View.VISIBLE);
        wv.invalidate();
    }

    public void hide() {
        wv.setVisibility(View.GONE);
    }

    public void toggleAdviceDetail(View v) {
        showAdviceDetails(!adviceDetails);
    }

    public void showAdviceDetails(boolean show) {
        if (show != adviceDetails) {

            // Fade to the proper scene.
            Transition mFadeTransition = new AutoTransition();
            if (show) {
                TransitionManager.go(openScene, mFadeTransition);
            } else {
                TransitionManager.go(closedScene, mFadeTransition);
            }

            for(int i = 0; i < adviceVisualizers.size(); i++) {
                if (show) {
                   adviceVisualizers.get(i).showText();
                } else {
                   adviceVisualizers.get(i).hideText();
                }
            }

            adviceDetails = show;
        }
    }
}
