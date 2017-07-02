package com.weatheradviceapp.models;


import com.weatheradviceapp.WeatherApplication;

import io.realm.RealmList;

public abstract class ActivityAdvice extends Advice {

    public abstract int getChipCaptionResource();

    public abstract int getChipColorResource();

    public final boolean checkInterest() {

        String name = this.getClass().getSimpleName();
        User user = User.getUser();
        RealmList<Interest> user_interests = user.getInterests();

        for (Interest interest : user_interests) {
            if (interest.getName().equals(name)) {
                return true;
            }
        }

        // When in demo mode always advice activities until chips check works
        return user.isEnabledDemoMode();
    }
}
