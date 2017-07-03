package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.ActivityAdvice;

public class WaterSports extends ActivityAdvice {

    /**
     * The WaterSports activity is based on the temperature and a nice wind speed.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        // Only when personal interest is selected
        if (this.checkInterest()) {
            return ((weather.temperature.getTemp() - 20) * 10) + (weather.wind.getSpeed() * 3.6f);
        }

        return 0.0f;
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.watersport_chip_icon;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_water_sports; }

    @Override
    public int getChipCaptionResource() {
        return R.string.watersport_chip;
    }

    @Override
    public int getChipColorResource() {
        return R.color.colorRed;
    }
}
