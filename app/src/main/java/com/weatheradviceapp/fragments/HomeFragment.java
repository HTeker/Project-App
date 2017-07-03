package com.weatheradviceapp.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.MainActivity;
import com.weatheradviceapp.R;
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

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class HomeFragment extends Fragment {

    private View thisView;
    private WeatherVisualizer wvToday;
    private WeatherVisualizer wvCalendar1;
    private WeatherVisualizer wvCalendar2;
    private int foreColor;
    private int shadowColor;

    private User user;

    private SwipeRefreshLayout swipeContainer;

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

        // Get user or create user if we don't have one yet.
        user = User.getOrCreateUser();

        Calendar cal = Calendar.getInstance();

        // One visualizer for the weather today.
        wvToday = new WeatherVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.weatherToday), new Weather(), cal.getTime());

        // Two visualizers for the upcoming calendar events.
        wvCalendar1 = new WeatherVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.weatherPlanning1), new Weather(), cal.getTime());
        wvCalendar2 = new WeatherVisualizer(inflater, (ViewGroup) thisView.findViewById(R.id.weatherPlanning2), new Weather(), cal.getTime());

        // Reload weather and calendar data on initial view load.
        refreshWeatherData(thisView);
        refreshCalendarData(thisView);
        
        // Pull-to-refresh initalization.
        swipeContainer = (SwipeRefreshLayout) thisView.findViewById(R.id.swipe_container);

        // Configure the refreshing colors.
        swipeContainer.setColorSchemeResources(
                R.color.colorPrimary,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Setup refresh listener which triggers new data loading.
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity activity = (MainActivity) getActivity();
                // Start job to fetch new weather data
                activity.fetchWeather();
                activity.fetchCalendarWeather();
            }
        });

        // Allowed nested scrolling so we can scroll inside the fragment
        // without triggering the swiper.
        swipeContainer.setNestedScrollingEnabled(true);

        return thisView;
    }

    /**
     * Shows the latest available weather data on screen. Call when new data
     * is retrieved from webservice.
     */
    public void refreshWeatherData() {
        // It's possible data is coming in when home fragmnet is not visible.
        if (getView() != null) {
            refreshWeatherData(getView().findViewById(R.id.fragment_home));
        }
    }

    /**
     * Shows the latest available calendar events on screen. Call when new data
     * is retrieved from webservice and calendar.
     */
    public void refreshCalendarData() {
        // It's possible data is coming in when home fragmnet is not visible.
        if (getView() != null) {
            refreshCalendarData(getView().findViewById(R.id.fragment_home));
        }
    }

    /**
     * Refreshes the data inside the current weather view.
     *
     * @param view
     *   The parent view.
     */
    public void refreshWeatherData(View view) {

        WeatherCondition latestWeatherCondition = WeatherCondition.getLatestWeatherCondition();

        // Latest weather could be empty when weather was never fetched.
        if (latestWeatherCondition != null) {
            Weather w = latestWeatherCondition.getWeather();

            // Set the image background for this weather condition.
            WeatherImageMapper wim = new WeatherImageMapper(w);
            view.setBackgroundResource(wim.getWeatherBackgroundResource());

            // Trigger the visualizer to show the weather details.
            wvToday.showWeatherData(w, latestWeatherCondition.getFetchDate(), null);

            // Set proper text colors so all texts are readable.
            foreColor = wim.getWeatherForegroundColor();
            shadowColor = wim.getWeatherShadowColor();
            setTextColor((ViewGroup) view, foreColor, shadowColor);
        }
    }

    /**
     * Refreshes the data inside the calendar views.
     *
     * @param view
     *   The parent view.
     */
    public void refreshCalendarData(View view) {
        // Variable to keep track of amount of displayed agenda events.
        int displayed_agenda = 0;

        // When demo mode is enabled, we just show the first two events from
        // realm, because in demo mode, we do not have any real calendars enabled.
        if (user.isEnabledDemoMode()) {
            Realm realm = Realm.getDefaultInstance();

            // Find all Realm calendar events.
            RealmResults<UserCalendarEvent> events = realm.where(UserCalendarEvent.class).findAllSorted("eventBeginDate", Sort.ASCENDING);

            // Loop through events.
            for (int i = 0; i < events.size(); i++) {
                // Make sure we only display 2 agendas.
                if (displayed_agenda == 2) {
                    break;
                }

                // Make sure this event actually has weather data.
                if (events.get(i).getWeather() == null) {
                    continue;
                }

                // Make sure our event date is later than now, we don't want
                // to display events that are happening/already happened.
                if (events.get(i).getEventBeginDate().getTime() > new Date().getTime()) {

                    // Apply weather details to the proper view.
                    if (displayed_agenda == 0) {
                        wvCalendar1.showWeatherData(events.get(i).getWeather(), events.get(i).getEventBeginDate(), events.get(i));
                        wvCalendar1.show();
                        // For some reason the icons are not tinted. When using hard coded values it
                        // works but using only the stored values it doesn't.
                        setTextColor((ViewGroup) thisView.findViewById(R.id.weatherPlanning1), 0, 0);
                        setTextColor((ViewGroup) thisView.findViewById(R.id.weatherPlanning1), foreColor, shadowColor);
                    }
                    if (displayed_agenda == 1) {
                        wvCalendar2.showWeatherData(events.get(i).getWeather(), events.get(i).getEventBeginDate(), events.get(i));
                        wvCalendar2.show();
                        setTextColor((ViewGroup) thisView.findViewById(R.id.weatherPlanning2), 0, 0);
                        setTextColor((ViewGroup) thisView.findViewById(R.id.weatherPlanning2), foreColor, shadowColor);
                    }

                    displayed_agenda++;
                }
            }
        } else {

            // Get enabled celanders for user.
            RealmList<UserCalendar> user_enabled_agendas = user.getAgendas();

            // Loop through all enabled agenda's.
            for (int i=0; i< user_enabled_agendas.size(); i++) {
                // Make sure we only display 2 agendas.
                if (displayed_agenda == 2) {
                    break;
                }

                UserCalendar calendar = user_enabled_agendas.get(i);

                // Get events for this calendars.
                RealmList<UserCalendarEvent> events = calendar.getEvents();

                // Loop through all events for this calendar.
                for (int i2 = 0; i2 < events.size(); i2++) {

                    // Make sure we only display 2 agendas.
                    if (displayed_agenda == 2) {
                        break;
                    }

                    // Make sure this event actually has weather data.
                    if (events.get(i2).getWeather() == null) {
                        continue;
                    }

                    // Make sure our event date is later than now, we don't want
                    // to display events that are happening/already happened.
                    if (events.get(i2).getEventBeginDate().getTime() > new Date().getTime()) {

                        // Apply weather details to the proper view.
                        if (displayed_agenda == 0) {
                            wvCalendar1.showWeatherData(events.get(i2).getWeather(), events.get(i2).getEventBeginDate(), events.get(i2));
                            wvCalendar1.show();
                            // For some reason the icons are not tinted. When using hard coded values it
                            // works but using only the stored values it doesn't.
                            setTextColor((ViewGroup) thisView.findViewById(R.id.weatherPlanning1), 0, 0);
                            setTextColor((ViewGroup) thisView.findViewById(R.id.weatherPlanning1), foreColor, shadowColor);
                        }
                        if (displayed_agenda == 1) {
                            wvCalendar2.showWeatherData(events.get(i2).getWeather(), events.get(i2).getEventBeginDate(), events.get(i2));
                            wvCalendar2.show();
                            setTextColor((ViewGroup) thisView.findViewById(R.id.weatherPlanning2), 0, 0);
                            setTextColor((ViewGroup) thisView.findViewById(R.id.weatherPlanning2), foreColor, shadowColor);
                        }

                        displayed_agenda++;
                    }
                }
            }
        }

        // Make sure to hide the views that don't have any data.
        if (displayed_agenda < 2) {
            wvCalendar2.hide();

            if (displayed_agenda < 1) {
                wvCalendar1.hide();
            }
        }

        // Make sure everything has the proper text/shadow color.
        setTextColor((ViewGroup) view, foreColor, shadowColor);
    }

    /**
     * Recursively loop through all views to find TextViews and change the text color.
     *
     * @param parent
     *   The parent view to loop through.
     * @param color
     *   The color to apply.
     * @param shadowColor
     *   The shadow color to apply.
     */
    public static void setTextColor(ViewGroup parent, int color, int shadowColor) {

        // Loop through all child views.
        for (int count=0; count < parent.getChildCount(); count++) {
            View view = parent.getChildAt(count);

            // If this is a TextView, set colors.
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                tv.setTextColor(color);
                tv.setShadowLayer(5, 2, 1, shadowColor);

            // If this is one of our weather icons, add the proper color.
            } else if (view instanceof ImageView) {
                if (view.getId() == R.id.iconCloud ||
                    view.getId() == R.id.iconRain ||
                    view.getId() == R.id.iconHumidity ||
                    view.getId() == R.id.iconWind ||
                    view.getId() == R.id.windDirection) {
                    ImageView imv = (ImageView) view;
                    imv.setImageDrawable(setTint(imv.getDrawable(), color));
                }
            // If this is an ViewGroup, recursively loop through children.
            } else if (view instanceof ViewGroup) {
                setTextColor((ViewGroup)view, color, shadowColor);
            }
        }
    }

    /**
     * Helper to wrap a drawable and set a tint to the wrapped drawable.
     *
     * @param d
     *   The drawable to set the tint on.
     * @param color
     *   The color of the tint.
     * @return
     *   The wrapped drawable.
     */
    public static Drawable setTint(Drawable d, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(d);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    /**
     * Helper method to disable the refresher.
     */
    public void disableRefresh() {

        // And reset the pull-to-refresh
        if (swipeContainer != null) {
            swipeContainer.setRefreshing(false);
        }
    }
}
