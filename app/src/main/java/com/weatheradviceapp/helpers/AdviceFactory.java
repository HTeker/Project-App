package com.weatheradviceapp.helpers;

import com.weatheradviceapp.models.ActivityAdvice;
import com.weatheradviceapp.models.Advice;
import com.weatheradviceapp.advice.*;
import com.weatheradviceapp.models.ClothingAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * The AdviceFactory has all Advice implementations registered and initiates the advices by name.
 */
public class AdviceFactory {

    public static final String[] adviceClasses = {
            "BeanieHat", "CottonClothing", "Gloves", "Raincoat", "SunGlasses", "Umbrella", "WoolClothing",
            "Barbecue", "Beach", "Bike", "Soccer", "WaterSports"};

    public enum Filter {
        ALL, ACTIVITY, CLOTHING
    }

    public static Advice getAdviceInstance(String adviceClass) {

        switch(adviceClass) {
            case "BeanieHat":
                return new BeanieHat();
            case "CottonClothing":
                return new CottonClothing();
            case "Gloves":
                return new Gloves();
            case "Raincoat":
                return new Raincoat();
            case "SunGlasses":
                return new SunGlasses();
            case "Umbrella":
                return new Umbrella();
            case "WoolClothing":
                return new WoolClothing();
            case "Barbecue":
                return new Barbecue();
            case "Beach":
                return new Beach();
            case "Bike":
                return new Bike();
            case "Soccer":
                return new Soccer();
            case "WaterSports":
                return new WaterSports();
            default:
                return null;
        }
    }

    public static List<Advice> getAllAdviceInstances(Filter filter) {
        List<Advice> result = new ArrayList<>();

        for (String className : adviceClasses) {
            Advice advice = getAdviceInstance(className);
            if (advice != null) {
                if (filter == Filter.ALL) {
                    result.add(advice);
                }
                if (filter == Filter.ACTIVITY && advice instanceof ActivityAdvice) {
                    result.add(advice);
                }
                if (filter == Filter.CLOTHING && advice instanceof ClothingAdvice) {
                    result.add(advice);
                }
            }
        }
        return result;
    }
}
