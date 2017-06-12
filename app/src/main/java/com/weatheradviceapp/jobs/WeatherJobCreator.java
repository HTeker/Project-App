package com.weatheradviceapp.jobs;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class WeatherJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case SyncWeatherJob.TAG:
                return new SyncWeatherJob();
            case DemoWeatherJob.TAG:
                return new DemoWeatherJob();
            default:
                return null;
        }
    }
}