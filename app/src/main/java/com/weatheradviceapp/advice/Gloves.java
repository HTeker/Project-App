package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;

public class Gloves extends Advice {

    /**
     * The sunglasses advice is based on the UV index, higher index is higher rating.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        if (weather.temperature.getTemp() > 5) {
            return 0F;
        } else {
            return 50 + (2 * (-weather.temperature.getTemp() + 5));
        }
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.advice_gloves;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_gloves; }

}
