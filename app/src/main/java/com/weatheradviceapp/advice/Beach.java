package com.weatheradviceapp.advice;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Advice;

public class Beach extends Advice {

    /**
     * The Beach activity is based on the temperature and clear sky.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return
     */
    @Override
    protected float calcWeatherScore(Weather weather) {
        // TODO: Only when personal interest is selected
        return 20 + ((weather.temperature.getTemp() - 20) * 10) + ((30 - weather.clouds.getPerc()) * 5);
    }

    @Override
    public int getAdviceIconResource() {
        return R.drawable.beach_chip_icon;
    }

    @Override
    public int getAdviceStringResource() { return R.string.advice_barbecue; }

}
