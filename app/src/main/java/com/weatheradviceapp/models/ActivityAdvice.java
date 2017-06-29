package com.weatheradviceapp.models;


import com.weatheradviceapp.WeatherApplication;

public abstract class ActivityAdvice extends Advice {

    protected final boolean checkInterest(int nameResID) {
        User user = User.getOrCreateUser();
        String name = WeatherApplication.getContext().getResources().getString(nameResID);

        for (Interest interest: user.getInterests()) {
            if (interest.getName().equals(name)) {
                return true;
            }
        }

        // When in demo mode always advice activities until chips check works
        return user.isEnabledDemoMode();
    }
}
