package com.weatheradviceapp.models;

import io.realm.RealmList;
import io.realm.RealmObject;

public class User extends RealmObject {
    private boolean enabledGPSLocation;
    private boolean enabledWiFiLocation;
    private boolean enabledAgendaSync;
    private boolean enabledAlarmSync;

    private double customLocationLat;
    private double customLocationLng;

    private RealmList<Interest> interests;

    public boolean isEnabledGPSLocation() {
        return enabledGPSLocation;
    }

    public void setEnabledGPSLocation(boolean enabledGPSLocation) {
        this.enabledGPSLocation = enabledGPSLocation;
    }

    public boolean isEnabledWiFiLocation() {
        return enabledWiFiLocation;
    }

    public void setEnabledWiFiLocation(boolean enabledWiFiLocation) {
        this.enabledWiFiLocation = enabledWiFiLocation;
    }

    public boolean isEnabledAgendaSync() {
        return enabledAgendaSync;
    }

    public void setEnabledAgendaSync(boolean enabledAgendaSync) {
        this.enabledAgendaSync = enabledAgendaSync;
    }

    public boolean isEnabledAlarmSync() {
        return enabledAlarmSync;
    }

    public void setEnabledAlarmSync(boolean enabledAlarmSync) {
        this.enabledAlarmSync = enabledAlarmSync;
    }

    public double getCustomLocationLat() {
        return customLocationLat;
    }

    public void setCustomLocationLat(double customLocationLat) {
        this.customLocationLat = customLocationLat;
    }

    public double getCustomLocationLng() {
        return customLocationLng;
    }

    public void setCustomLocationLng(double customLocationLng) {
        this.customLocationLng = customLocationLng;
    }

    public RealmList<Interest> getInterests() {
        return interests;
    }

    public void setInterests(RealmList<Interest> interests) {
        this.interests = interests;
    }
}
