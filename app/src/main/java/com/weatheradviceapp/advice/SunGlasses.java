package com.weatheradviceapp.advice;

import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;
import com.survivingwithandroid.weather.lib.model.Weather;

public class SunGlasses extends Advice {

    @Override
    protected double calcWeatherScore(Weather weather) {
        return 2.0;
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.umbrella_icon;
    }

    @Override
    public int getAdviceStringResource() {
        return R.string.weather_text_000;
    }

}
