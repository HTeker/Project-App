package com.weatheradviceapp;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.fragments.HomeFragment;
import com.weatheradviceapp.fragments.SettingsFragment;
import com.weatheradviceapp.helpers.AdviceFactory;
import com.weatheradviceapp.helpers.WeatherAdviceGenerator;
import com.weatheradviceapp.jobs.DemoCalendarJob;
import com.weatheradviceapp.jobs.DemoWeatherJob;
import com.weatheradviceapp.jobs.SyncCalendarJob;
import com.weatheradviceapp.jobs.SyncWeatherJob;
import com.weatheradviceapp.models.Advice;
import com.weatheradviceapp.models.User;
import com.weatheradviceapp.models.WeatherCondition;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private JobManager mJobManager;
    private User user;
    private BroadcastReceiver mMessageReceiver;

    private static final String[] REQUIRED_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CALENDAR
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get user or create user if we don't have one yet.
        user = User.getOrCreateUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Require proper permissions.
        if (!hasRequiredPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(REQUIRED_PERMS, 1);
            }
        }

        // Initialize navigation drawer.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Allow intents for data updates.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncWeatherJob.WEATHER_AVAILABLE);
        intentFilter.addAction(SyncCalendarJob.WEATHER_AVAILABLE);

        // Intent receiver.
        mMessageReceiver = new WeatherReceiver(new Handler());
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);

        // Job manager for background processing.
        mJobManager = JobManager.instance();

        // Reset the job manager if we had any scheduled jobs.
        mJobManager.cancelAll();

        // Fetch weather/calendar data.
        fetchWeather();
        scheduleWeatherFetching();

        // Schedule weather/calendar fetchers for background processing.
        fetchCalendarWeather();
        scheduleCalendarWeatherFetching();

        // Init home fragment by default.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new HomeFragment(), "home");
        ft.commit();
    }

    private class WeatherReceiver extends BroadcastReceiver {

        private final Handler uiHandler; // Handler used to execute code on the UI thread

        public WeatherReceiver(Handler handler) {
            this.uiHandler = handler;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(SyncWeatherJob.WEATHER_AVAILABLE)) {
                // Post the UI updating code to our Handler.
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
                        if (homeFragment != null) {
                            homeFragment.refreshWeatherData();
                            homeFragment.disableRefresh();
                        }

                        checkForNotifications();
                    }
                });
            }

            if (intent.getAction().equalsIgnoreCase(SyncCalendarJob.WEATHER_AVAILABLE)) {
                // Post the UI updating code to our Handler.
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
                        if (homeFragment != null) {
                            homeFragment.refreshCalendarData();
                            homeFragment.disableRefresh();
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    public void fetchWeather() {
        new JobRequest.Builder(user.isEnabledDemoMode() ? DemoWeatherJob.TAG : SyncWeatherJob.TAG)
                .setExecutionWindow(3_000L, 4_000L)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.LINEAR)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setPersisted(true)
                .build()
                .schedule();
    }

    private void scheduleWeatherFetching() {
        new JobRequest.Builder(SyncWeatherJob.TAG)
                .setPeriodic(JobRequest.MIN_INTERVAL, JobRequest.MIN_FLEX)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPersisted(true)
                .build()
                .schedule();
    }

    public void fetchCalendarWeather() {
        new JobRequest.Builder(user.isEnabledDemoMode() ? DemoCalendarJob.TAG : SyncCalendarJob.TAG)
                .setExecutionWindow(3_000L, 4_000L)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.LINEAR)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setPersisted(true)
                .build()
                .schedule();
    }

    private void scheduleCalendarWeatherFetching() {
        new JobRequest.Builder(SyncCalendarJob.TAG)
                .setPeriodic(JobRequest.MIN_INTERVAL, JobRequest.MIN_FLEX)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPersisted(true)
                .build()
                .schedule();
    }

    @Override
    public void onBackPressed() {

        // Fix back back press, when backbutton is pressed, see if we had a fragment on the stack.
        // Imitate activity behaviour.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                displayView(id);
                break;

            default:
                displayView(id);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean hasRequiredPermissions() {
        for (String permision : REQUIRED_PERMS) {
            if (!hasPermission(permision)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, perm));
    }

    /**
     * Helper to display a new fragment by menu item id.
     *
     * @param viewId
     *   The menu item ID to display the fragment for.
     */
    public void displayView(int viewId) {

        Fragment fragment = null;
        String title = getString(R.string.weather_app_name);
        switch (viewId) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                title = getString(R.string.title_home);
                break;

            case R.id.nav_settings:
                fragment = new SettingsFragment();
                title = getString(R.string.title_settings);
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.addToBackStack(title);
            ft.replace(R.id.content_frame, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private PendingIntent preparePushNotification() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return resultPendingIntent;
    }

    public void showPushNotification(Advice advice, int messageId) {
        // @Gabriel, hier is een kopie van jouw code maar dan geparametriseerd met de advice. Omdat
        // alle notifications het zelfde doen hoef je alleen de eigenschappen van de advice te
        // gebruiken. Als je iets anders wilt weergeven dan moet je dat op de advice class toevoegen
        // en in de concrete implementaties geef je dan de gewenste waarde terug. Bijvoorbeeld een
        // andere tekst bij de herinnering.
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(advice.getAdviceIconResource())
                .setContentTitle(getString(R.string.weather_app_name))
                .setContentText(advice.toString())
                .setAutoCancel(true)
                .setContentIntent(preparePushNotification());

        mNotificationManager.notify(messageId, nBuilder.build());

        // Nu nog uitvinden wanneer we de notificatie moeten aanroepen, ik denk dat we dat moeten
        // doen als we niet weer ontvangen.
    }

    public void checkForNotifications() {

        // Dit is van de HomeFragment.refreshWeatherData() gepakt. Ik weet nog niet wat de bedoeling
        // precies is met de notifications maar ik kan me voorstellen dat deze code nog verplaatst
        // moet worden naar een plek waarbij de adviezen maar 1 maal worden gegenereerd.
        WeatherCondition latestWeatherCondition = WeatherCondition.getLatestWeatherCondition();

        if (latestWeatherCondition != null) {

            ArrayList<Weather> allWeathers = new ArrayList<>();
            allWeathers.add(latestWeatherCondition.getWeather());

            // Generate advice for all weather conditions
            WeatherAdviceGenerator advGen = new WeatherAdviceGenerator(allWeathers, AdviceFactory.Filter.CLOTHING);

            // En nu dus de eerste advice pakken en controleren of er wel positief advies is
            if (advGen.size() > 0) {
                Advice activity = advGen.get(0);
                if (activity.getScore() > 40.0f) {
                    showPushNotification(activity, 1);
                }
            }
        }
    }
}