package com.weatheradviceapp.models;

import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.WeatherApplication;

import java.util.List;

/**
 * The abstract Advice class is the base class for weather advice. It stores the best score and
 * weather condition on which the score is accomplished. Each concrete Advice class should implement
 * a formula to calculate a score on a weather condition. Also the references to the icon resource
 * and the string resource should be implemented in the concrete classes.
 *
 * Register all concrete classes in the helpers.AdviceFactory.
 */
public abstract class Advice {

    private Weather weather;
    private float score = 0.0f;

    /**
     * Find the highest score on the weather conditions
     *
     * @param weatherConditions Multiple weather conditions to test score on
     */
    public void saveBestScore(Iterable<Weather> weatherConditions) {
        // Select weather with highest score
        for (Weather w : weatherConditions) {
            saveBestScore(w);
        }
    }

    /**
     * Save only the highest score and weather condition.
     *
     * @param weather The weather condition to calculate the score on.
     */
    public void saveBestScore(Weather weather) {
        float score = calcWeatherScore(weather);
        if (this.score < score || this.weather == null) {
            this.score = score;
            this.weather = weather;
        }
    }

    /**
     * Returns the best score calculated on the given weather conditions.
     *
     * @return The best advice score.
     */
    public float getScore() {
        return score;
    }

    /**
     * Returns the weather condition on which the score is calculated. Only the best score is saved.
     *
     * @return The weather on which the score is calculated.
     */
    public Weather getScoreWeather() {
        return weather;
    }

    /**
     * Implement the score calculation for the weather condition in the concrete class. In general
     * Normal conditions have a scale between 0.0 and 100.0 but extreme conditions can exceed that
     * scale.
     *
     * @param weather The weather condition to calculate the advice score on.
     * @return The advice score, higher is better.
     */
    protected abstract float calcWeatherScore(Weather weather);

    public abstract int getAdviceIconResource();

    public abstract int getAdviceStringResource();

    @Override
    public String toString() {
        if (weather != null) {
            return WeatherApplication.getContext().getString(getAdviceStringResource(), weather.location.getCity());
        }
        return "";
    }
}
