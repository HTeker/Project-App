package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.ActivityAdvice;

public class Soccer extends ActivityAdvice {

    /**
     * The Soccer activity is based on the temperature, wind and rain predictions. Not too much wind
     * and less rain will result in a better score.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        // Only when personal interest is selected
        if (this.checkInterest()) {
            return (float) (Math.pow(weather.temperature.getTemp() - 17f, 2f) / -4 + 40) + // Temp range with optimum score at 175 C
                    (30 - weather.rain[0].getChance()) + // Above 30% chance rain degrades the score
                    (35 - (weather.wind.getSpeed() * 3.6f)); // Wind speed above 25 kmph degrades score
        }

        return 0.0f;
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.soccer_chip_icon;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_soccer; }

    @Override
    public int getChipCaptionResource() {
        return R.string.soccer_chip;
    }

    @Override
    public int getChipColorResource() {
        return R.color.colorGreen;
    }
}
