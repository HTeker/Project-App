package com.weatheradviceapp.helpers;


import com.survivingwithandroid.weather.lib.model.Weather;
import com.weatheradviceapp.models.Advice;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WeatherAdviceGenerator  {

    private List<Advice> adviceList;

    /**
     * The WeatherAdviceGenerator runs all the weather conditions against the Advice implementations
     * to get the highest score for a weather condition. The list of advices is then sorted based on
     * score.
     *
     * @param weatherConditions
     */
    public WeatherAdviceGenerator(Iterable<Weather> weatherConditions, AdviceFactory.Filter filter) {
        adviceList = AdviceFactory.getAllAdviceInstances(filter);

        for (Advice advice : adviceList) {
            advice.saveBestScore(weatherConditions);
        }

        Collections.sort(adviceList, new Comparator<Advice>() {
            @Override
            public int compare(Advice a, Advice b) {
                // Sort in reverse order, highest first, lowest at end of list
                return a.getScore() > b.getScore() ? -1 : a.getScore() < b.getScore() ? 1 : 0;
            }
        });
    }

    public int size() {
        return adviceList.size();
    }

    public Advice get(int index) {
        return adviceList.get(index);
    }
}
