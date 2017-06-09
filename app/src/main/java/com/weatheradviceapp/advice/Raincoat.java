package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;

public class Raincoat extends Advice {

    /**
     * The sunglasses advice is based on the rain prediction. Wind also ads score.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        return weather.rain[0].getChance() + (weather.wind.getSpeed() * 3.6f);
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.advice_raincoat;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_raincoat; }

}
