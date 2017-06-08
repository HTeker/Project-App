package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;

public class WoolClothing extends Advice {

    /**
     * The umbrella score is based on the rain forecast but with too much wind the score goes down.
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
