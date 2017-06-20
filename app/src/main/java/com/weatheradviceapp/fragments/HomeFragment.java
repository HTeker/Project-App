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
import com.weatheradviceapp.models.User;
import com.weatheradviceapp.models.UserCalendar;
import com.weatheradviceapp.models.UserCalendarEvent;
import com.weatheradviceapp.models.WeatherCondition;
import com.weatheradviceapp.views.AdviceVisualizer;
import com.weatheradviceapp.views.WeatherVisualizer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;

public class HomeFragment extends Fragment {

    private WeatherVisualizer wvToday;
    private WeatherVisualizer wvCalendar1;
    private WeatherVisualizer wvCalendar2;

    private List<AdviceVisualizer> adviceVisualizers = new ArrayList<>();
    private User user;

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

        // Get user or create user if we don't have one yet.
        user = User.getOrCreateUser();

        Calendar cal = Calendar.getInstance();
        wvToday = new WeatherVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.weatherToday), new Weather(), cal.getTime());

        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice1)));
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice2)));
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice3)));
        adviceVisualizers.add(new AdviceVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.advice4)));

        wvCalendar1 = new WeatherVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.weatherPlanning1), new Weather(), cal.getTime());
        wvCalendar2 = new WeatherVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.weatherPlanning2), new Weather(), cal.getTime());

        refreshWeatherData(thisView);
        refreshCalendarData(thisView);

        return thisView;
    }

    /**
     * Shows the latest available weather data on screen. Call when new data is retrieved from
     * webservice.
     */
    public void refreshWeatherData() {
        refreshWeatherData(getView().findViewById(R.id.fragment_home));
    }

    /**
     * Shows the latest available weather data on screen. Call when new data is retrieved from
     * webservice.
     */
    public void refreshCalendarData() {
        refreshCalendarData(getView().findViewById(R.id.fragment_home));
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

    public void refreshCalendarData(View view) {
        RealmList<UserCalendar> user_enabled_agendas = user.getAgendas();

        int displayed_agenda = 0;

        // Loop through all enabled agenda's.
        for (int i=0; i< user_enabled_agendas.size(); i++) {
            if (displayed_agenda == 2) {
                break;
            }

            UserCalendar calendar = user_enabled_agendas.get(i);
            RealmList<UserCalendarEvent> events = calendar.getEvents();
            for (int i2 = 0; i2 < events.size(); i2++) {
                if (displayed_agenda == 2) {
                    break;
                }

                if (events.get(i2).getWeather() == null) {
                    continue;
                }

                if (events.get(i2).getEventBeginDate().getTime() > new Date().getTime()) {
                    if (displayed_agenda == 0) {
                        wvCalendar1.showWeatherData(events.get(i2).getWeather(), events.get(i2).getEventBeginDate());
                        wvCalendar1.setText(events.get(i2).getTitle());
                        wvCalendar1.setLocation(events.get(i2).getLocationTitle());
                        wvCalendar1.show();
                    }
                    if (displayed_agenda == 1) {
                        wvCalendar2.showWeatherData(events.get(i2).getWeather(), events.get(i2).getEventBeginDate());
                        wvCalendar2.setText(events.get(i2).getTitle());
                        wvCalendar2.setLocation(events.get(i2).getLocationTitle());
                        wvCalendar2.show();
                    }

                    displayed_agenda++;
                }
            }
        }

        if (displayed_agenda == 0) {
            wvCalendar1.hide();
            wvCalendar2.hide();
        }

        if (displayed_agenda == 1) {
            wvCalendar2.hide();
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
