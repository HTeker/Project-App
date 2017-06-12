package com.weatheradviceapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.weatheradviceapp.fragments.AgendaFragment;
import com.weatheradviceapp.fragments.SettingsFragment;
import com.weatheradviceapp.jobs.DemoWeatherJob;
import com.weatheradviceapp.jobs.SyncWeatherJob;
import com.weatheradviceapp.models.User;

import com.weatheradviceapp.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SwipeRefreshLayout swipeContainer;
    private JobManager mJobManager;

    private ConstraintSet normalLayout = new ConstraintSet();
    private ConstraintSet adviceDetailLayout = new ConstraintSet();
    private boolean adviceDetails = false;
    private boolean demoMode = false;


    private static final String[] REQUIRED_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CALENDAR
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        normalLayout.clone(getApplicationContext(), R.layout.fragment_home);
        adviceDetailLayout.clone(getApplicationContext(), R.layout.fragment_advice);


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

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("new-weather-available"));

        mJobManager = JobManager.instance();
        // Reset.
        mJobManager.cancelAll();

        // Pull-to-refresh
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.colorPrimary,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Start job to fetch new weather data
                fetchWeather();
            }
        });

        // Just now fetch weather data, so we're sure the swipeContainer is assigned
        fetchWeather();
        scheduleWeatherFetching();

        // Init home fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new HomeFragment(), "home");
        ft.commit();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            HomeFragment homeFragment = (HomeFragment)getSupportFragmentManager().findFragmentByTag("home");
            if (homeFragment != null && intent.getAction() == SyncWeatherJob.WEATHER_AVAILABLE) {
                homeFragment.refreshWeatherData();
            }

            // And reset the pull-to-refresh
            if (swipeContainer != null) {
                swipeContainer.setRefreshing(false);
            }
        }
    };

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private void fetchWeather() {
        User currentUser = User.getUser();

        if (currentUser != null) {
            new JobRequest.Builder(demoMode ? DemoWeatherJob.TAG : SyncWeatherJob.TAG)
                    .setExecutionWindow(3_000L, 4_000L)
                    .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.LINEAR)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setPersisted(true)
                    .build()
                    .schedule();
        }
    }

    private void scheduleWeatherFetching() {
        User currentUser = User.getUser();

        if (currentUser != null) {
            new JobRequest.Builder(SyncWeatherJob.TAG)
                    .setPeriodic(JobRequest.MIN_INTERVAL, JobRequest.MIN_FLEX)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setPersisted(true)
                    .build()
                    .schedule();
        }
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

        switch(id) {
            case R.id.nav_demo_mode:
                demoMode = !demoMode;
                item.setChecked(demoMode);
                break;
            case R.id.nav_home:
            case R.id.nav_my_advice:
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
        return(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, perm));
    }

    public void displayView(int viewId) {

        Fragment fragment = null;
        String title = getString(R.string.weather_app_name);

        switch (viewId) {
            case R.id.nav_home:
            case R.id.nav_my_advice:
                fragment = new HomeFragment();
                title = getString(R.string.title_home);
                break;

            case R.id.nav_agenda:
                fragment = new AgendaFragment();
                title = getString(R.string.title_agenda);
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

    public void toggleAdviceDetail(View v) {
        showAdviceDetails(!adviceDetails);
    }

    public void showAdviceDetails(boolean show) {
        if (show != adviceDetails) {
            ConstraintLayout homeFragment = (ConstraintLayout) findViewById(R.id.fragment_home);
            if (homeFragment != null) {
                TransitionManager.beginDelayedTransition(homeFragment);
                if (show) {
                    adviceDetailLayout.applyTo(homeFragment);
                } else {
                    normalLayout.applyTo(homeFragment);
                }
                adviceDetails = show;
            }
        }
    }
}
