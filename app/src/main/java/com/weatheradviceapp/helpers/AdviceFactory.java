package com.weatheradviceapp.helpers;

import com.weatheradviceapp.models.Advice;
import com.weatheradviceapp.advice.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The AdviceFactory has all Advice implementations registered and initiates the advices by name.
 */
public class AdviceFactory {

    public static final String[] adviceClasses = {"BeanieHat", "CottonClothing", "Gloves", "Raincoat", "SunGlasses", "Umbrella", "WoolClothing"};

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
            default:
                return null;
        }
    }

    public static List<Advice> getAllAdviceInstances() {
        ArrayList<Advice> result = new ArrayList<>();

        for (String className : adviceClasses) {
            Advice advice = getAdviceInstance(className);
            if (advice != null) {
                result.add(advice);
            }
        }
        return result;
    }
}
