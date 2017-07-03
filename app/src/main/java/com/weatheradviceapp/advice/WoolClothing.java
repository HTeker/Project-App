package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.ClothingAdvice;

public class WoolClothing extends ClothingAdvice {

    /**
     * The WoolClothing score is based on the temperature. Temperatures below 12 degrees gets a
     * standard advice ratio.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        if (weather.temperature.getTemp() < 12) {
            return 60.0f;
        } else {
            return 0.0f;
        }
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.advice_wool;
    }

    @Override
    public int getAdviceStringResource() {
        return R.string.advice_wool;
    }
}
