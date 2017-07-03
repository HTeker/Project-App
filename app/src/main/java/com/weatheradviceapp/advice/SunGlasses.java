package com.weatheradviceapp.advice;

import com.weatheradviceapp.R;
import com.weatheradviceapp.models.ClothingAdvice;
import com.survivingwithandroid.weather.lib.model.Weather;

public class SunGlasses extends ClothingAdvice {

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
