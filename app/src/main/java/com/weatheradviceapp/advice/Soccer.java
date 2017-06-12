package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;

public class Soccer extends Advice {

    /**
     * The Soccer activity is based on the temperature, wind and rain predictions. Not too much wind
     * and less rain will result in a better score.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        // TODO: Only when personal interest is selected
        return (float)(Math.pow(weather.temperature.getTemp()-17f, 2f) / -4 + 40) + // Temp range with optimum score at 175 C
               (30 - weather.rain[0].getChance()) + // Above 30% chance rain degrades the score
               (35 - (weather.wind.getSpeed() * 3.6f)); // Wind speed above 25 kmph degrades score
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.soccer_chip_icon;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_soccer; }

}
