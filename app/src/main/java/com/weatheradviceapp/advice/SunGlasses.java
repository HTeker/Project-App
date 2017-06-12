package com.weatheradviceapp.advice;

import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;
import com.survivingwithandroid.weather.lib.model.Weather;

public class SunGlasses extends Advice {

    /**
     * The sunglasses advice is based on the UV index, higher index is higher rating.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        return 100F - weather.clouds.getPerc();
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.advice_sunglasses;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_sunglasses; }

}
