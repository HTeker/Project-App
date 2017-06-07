package com.weatheradviceapp.helpers;

import com.weatheradviceapp.models.Advice;
import com.weatheradviceapp.advice.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The AdviceFactory has all Advice implementations registered and initiates the advices by name.
 */
public class AdviceFactory {

    public static final String[] adviceClasses = {"SunGlasses", "Umbrella"};

    public static Advice getAdviceInstance(String adviceClass) {

        switch(adviceClass) {
            case "SunGlasses":
                return new SunGlasses();
            case "Umbrella":
                return new Umbrella();
            default:
                return null;
        }
    }

    public static List<Advice> getAdviceInstances() {
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
