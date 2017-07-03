package com.weatheradviceapp.models;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class User extends RealmObject {
    private boolean enabledGPSLocation;
    private boolean enabledWiFiLocation;
    private boolean enabledAgendaSync;
    private boolean enabledAlarmSync;
    private boolean enabledDemoMode;
    private RealmList<Network> wifiNetworks = new RealmList<Network>();

    private boolean setOwnPosition;
    private double customLocationLat;
    private double customLocationLng;

    private RealmList<Interest> interests;
    private RealmList<UserCalendar> agendas;

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

    public boolean isEnabledDemoMode() {
        return enabledDemoMode;
    }

    public void setEnabledDemoMode(boolean enabledDemoMode){
        this.enabledDemoMode = enabledDemoMode;
    }

    public RealmList<Network> getWifiNetworks(){
        return wifiNetworks;
    }

    public void addWifiNetwork(Network network) {
        wifiNetworks.add(network);
    }

    public void removeWifiNetwork(Network network) {
        wifiNetworks.remove(network);
    }

    public boolean isSetOwnPosition() {
        return setOwnPosition;
    }

    public void setSetOwnPosition(boolean setOwnPosition) {
        this.setOwnPosition = setOwnPosition;
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

    public RealmList<UserCalendar> getAgendas() {
        return agendas;
    }

    public void setAgendas(RealmList<UserCalendar> agendas) {
        this.agendas = agendas;
    }

    public static User getUser() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<User> list = realm.where(User.class).findAll();
        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    public static User getOrCreateUser() {
        User user = getUser();

        if (user == null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            user = realm.createObject(User.class);
            realm.commitTransaction();
        }

        return user;
    }
}
