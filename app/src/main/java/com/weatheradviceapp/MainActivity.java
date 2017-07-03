package com.weatheradviceapp;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.fragments.SettingsFragment;
import com.weatheradviceapp.jobs.DemoCalendarJob;
import com.weatheradviceapp.jobs.DemoWeatherJob;
import com.weatheradviceapp.jobs.SyncCalendarJob;
import com.weatheradviceapp.jobs.SyncWeatherJob;
import com.weatheradviceapp.models.User;

import com.weatheradviceapp.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private JobManager mJobManager;
    private User user;
    private BroadcastReceiver mMessageReceiver;
    private Weather currentWeather;

    private static final String[] REQUIRED_PERMS={
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

        if(!hasRequiredPermissions()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(REQUIRED_PERMS, 1);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncWeatherJob.WEATHER_AVAILABLE);
        intentFilter.addAction(SyncCalendarJob.WEATHER_AVAILABLE);

        mMessageReceiver = new WeatherReceiver(new Handler());
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);

        mJobManager = JobManager.instance();
        // Reset.
        mJobManager.cancelAll();

        pushNotficationCaller();

        // Just now fetch weather data, so we're sure the swipeContainer is assigned
        fetchWeather();
        scheduleWeatherFetching();
        fetchCalendarWeather();
        scheduleCalendarWeatherFetching();

        // Init home fragment
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
                // Post the UI updating code to our Handler
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
                        if (homeFragment != null) {
                            homeFragment.refreshWeatherData();
                            homeFragment.disableRefresh();
                        }
                    }
                });
            }

            if (intent.getAction().equalsIgnoreCase(SyncCalendarJob.WEATHER_AVAILABLE)) {
                // Post the UI updating code to our Handler
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
                // Not working because of new fragment initialization in displayView()
                //showAdviceDetails(id == R.id.nav_my_advice);
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
        for (String permision : REQUIRED_PERMS){
            if (!hasPermission(permision)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, perm));
    }

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

    public PendingIntent preparePushNotification(){

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        return resultPendingIntent;
    }

    public void showDemoPushNotification() {

        if(user.isEnabledDemoMode()){
            NotificationCompat.Builder demoNotificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.weather_advice)
                            .setContentTitle("Bobs Weather Advice")
                            .setContentText("Don't forget your Umbrella, Coat and Gloves")
                            .setAutoCancel(true);

            demoNotificationBuilder.setContentIntent(preparePushNotification());
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int mId = 1;
            mNotificationManager.notify(mId, demoNotificationBuilder.build());
        }
    }

    public void showBarbequeNotification() {

        if (user.isEnabledBarbecueChip()){
            NotificationCompat.Builder barbecueNotificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.weather_advice)
                            .setContentTitle("Bobs Weather Advice")
                            .setContentText("It's a nice weather to barbecue")
                            .setAutoCancel(true);

            barbecueNotificationBuilder.setContentIntent(preparePushNotification());
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int mId = 2;
            mNotificationManager.notify(mId, barbecueNotificationBuilder.build());
        }
    }

    public void showSoccerNotification() {

        if (user.isEnabledSoccerChip()){
            NotificationCompat.Builder soccerNotificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.weather_advice)
                            .setContentTitle("Bobs Weather Advice")
                            .setContentText("It's a nice weather to play soccer")
                            .setAutoCancel(true);

            soccerNotificationBuilder.setContentIntent(preparePushNotification());
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int mId = 3;
            mNotificationManager.notify(mId, soccerNotificationBuilder.build());
        }
    }

    public void showBeachNotification() {

        if (user.isEnabledBeachChip()){
            NotificationCompat.Builder beachNotificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.weather_advice)
                            .setContentTitle("Bobs Weather Advice")
                            .setContentText("It's a nice weather to go to the beach")
                            .setAutoCancel(true);

            beachNotificationBuilder.setContentIntent(preparePushNotification());
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int mId = 4;
            mNotificationManager.notify(mId, beachNotificationBuilder.build());
        }
    }

    public void showBikeNotification() {

        if (user.isEnabledBikeChip()){
            NotificationCompat.Builder bikeNotificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.weather_advice)
                            .setContentTitle("Bobs Weather Advice")
                            .setContentText("It's a nice weather for biking")
                            .setAutoCancel(true);

            bikeNotificationBuilder.setContentIntent(preparePushNotification());
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int mId = 5;
            mNotificationManager.notify(mId, bikeNotificationBuilder.build());
        }
    }

    public void showWaterSportNotification() {

        if (user.isEnabledWaterSportChip()){
            NotificationCompat.Builder waterSportNotificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.weather_advice)
                            .setContentTitle("Bobs Weather Advice")
                            .setContentText("It's a nice weather for water sporting")
                            .setAutoCancel(true);

            waterSportNotificationBuilder.setContentIntent(preparePushNotification());
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int mId = 6;
            mNotificationManager.notify(mId, waterSportNotificationBuilder.build());
        }
    }

    public void pushNotficationCaller() {

        showDemoPushNotification();
        showBarbequeNotification();
        showBeachNotification();
        showBikeNotification();
        showSoccerNotification();
        showWaterSportNotification();
    }
}