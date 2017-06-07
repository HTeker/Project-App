package com.weatheradviceapp.models;

import com.survivingwithandroid.weather.lib.model.Weather;
import java.util.List;

/**
 * The abstract Advice class is the base class for weather advice. It stores the best score and
 * weather condition on which the score is accomplished. Each concrete Advice class should implement
 * a formula to calculate a score on a weather condition. Also the references to the icon resource
 * and the string resource should be implemented in the concrete classes.
 *
 * Register all concrete classes in the AdviceFactory.
 */
public abstract class Advice {

    private Weather weather;
    private double score = 0.0;

    /**
     * Find the highest score on the weather conditions
     *
     * @param weatherConditions
     */
    public void calcBestScore(Iterable<Weather> weatherConditions) {
        for (Weather w : weatherConditions) {
            double score = calcWeatherScore(w);
            if (this.score < score) {
                this.score = score;
                this.weather = w;
            }
        }
    }

    public double getScore() {
        return score;
    }

    public Weather getScoreWeather() {
        return weather;
    }

    protected abstract double calcWeatherScore(Weather weather);

    public abstract int getAdviceIconResource();

    public abstract int getAdviceStringResource();
}
