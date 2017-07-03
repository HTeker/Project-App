package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.ClothingAdvice;

public class CottonClothing extends ClothingAdvice {

    /**
     * The CottonClothing score is based on the temperature above 20 C.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        if (weather.temperature.getTemp() > 20) {
            return 60.0f;
        } else {
            return 0.0f;
        }
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.advice_cotton;
    }

    @Override
    public int getAdviceStringResource() {
        return R.string.advice_cotton;
    }
}
