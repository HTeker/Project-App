package com.weatheradviceapp;

import android.app.Application;

import com.evernote.android.job.JobManager;
import com.weatheradviceapp.jobs.WeatherJobCreator;

import io.realm.Realm;

public class WeatherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        JobManager.create(this).addJobCreator(new WeatherJobCreator());
    }
}
