package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.ActivityAdvice;

public class Beach extends ActivityAdvice {

    /**
     * The Beach activity is based on the temperature and clear sky.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        // Only when personal interest is selected
        if (this.checkInterest()) {
            return 20 + ((weather.temperature.getTemp() - 20) * 10) + ((30 - weather.clouds.getPerc()) * 5);
        }

        return 0.0f;
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.beach_chip_icon;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_barbecue; }

    @Override
    public int getChipCaptionResource() {
        return R.string.beach_chip;
    }

    @Override
    public int getChipColorResource() {
        return R.color.colorBeige;
    }
}
