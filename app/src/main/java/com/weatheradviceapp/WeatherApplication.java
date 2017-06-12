package com.weatheradviceapp;

import android.app.Application;
import android.content.Context;

import com.evernote.android.job.JobManager;
import com.weatheradviceapp.jobs.WeatherJobCreator;

import io.realm.Realm;

public class WeatherApplication extends Application {

    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Realm.init(this);
        JobManager.create(this).addJobCreator(new WeatherJobCreator());
    }

    /**
     * Singleton design pattern for the application
     *
     * @return The main application instance
     */
    public static Application getInstance() {
        return instance;
    }

    /**
     * Globally available Context
     *
     * @return The main application context
     */
    public static Context getContext() {
        return getInstance().getApplicationContext();
    }
}
