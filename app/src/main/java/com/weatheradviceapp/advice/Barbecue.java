package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.ActivityAdvice;

public class Barbecue extends ActivityAdvice {

    /**
     * The Barbecue activity is based on the temprerature and rain prediction. Higher temperature
     * and lower rain chance results in a higher rating.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        // Only when personal interest is selected
        if (this.checkInterest()) {
            return (weather.temperature.getTemp() >= 18 ? 40 : 0) + ((15 - weather.rain[0].getChance()) * 2);
        }

        return 0.0f;
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.barbecue_chip_icon;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_barbecue; }

    @Override
    public int getChipCaptionResource() {
        return R.string.barbecue_chip;
    }

    @Override
    public int getChipColorResource() {
        return R.color.colorGrey;
    }

}
