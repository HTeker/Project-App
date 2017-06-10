package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;

public class Barbecue extends Advice {

    /**
     * The Barbecue activity is based on the temprerature and rain prediction. Higher temperature
     * and lower rain chance results in a higher rating.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        // TODO: Only when personal interest is selected
        return (weather.temperature.getTemp() >= 18 ? 40 : 0) + ((15 - weather.rain[0].getChance()) * 2);
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.barbecue_chip_icon;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_barbecue; }

}
