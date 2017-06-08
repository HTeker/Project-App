package com.weatheradviceapp.advice;

import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;
import com.survivingwithandroid.weather.lib.model.Weather;

public class Umbrella extends Advice {

    /**
     * The umbrella score is based on the rain forecast but with too much wind the score goes down.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        if (weather.wind.getSpeed() > 60 || weather.rain.length == 0) {
            // No score on strong wind
            return 0.0f;
        }
        return weather.rain[0].getChance() - (weather.wind.getSpeed() * 3.6f);
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.advice_umbrella;
    }

    @Override
    public int getAdviceStringResource() {
        return R.string.advice_umbrella;
    }
}
