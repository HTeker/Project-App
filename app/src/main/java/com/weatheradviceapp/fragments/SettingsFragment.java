package com.weatheradviceapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.weatheradviceapp.R;
import com.weatheradviceapp.models.User;

import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsFragment extends Fragment {

    private Realm realm;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a Realm instance for this thread
        realm = Realm.getDefaultInstance();

        // Create user if we don't have one yet.
        final RealmResults<User> users = realm.where(User.class).findAll();
        if (users.size() == 0) {
            realm.beginTransaction();
            user = realm.createObject(User.class);
            realm.commitTransaction();
        } else {
            user = users.get(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch gps_switch = (Switch) view.findViewById(R.id.enabled_gps_location_switch);
        if (user.isEnabledGPSLocation()) {
            gps_switch.setChecked(true);
        }

        gps_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                user.setEnabledGPSLocation(isChecked);
                realm.commitTransaction();
            }
        });

        Switch wifi_switch = (Switch) view.findViewById(R.id.enabled_wifi_location_switch);
        if (user.isEnabledWiFiLocation()) {
            wifi_switch.setChecked(true);
        }

        wifi_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                user.setEnabledWiFiLocation(isChecked);
                realm.commitTransaction();
            }
        });

        Switch agenda_sync_switch = (Switch) view.findViewById(R.id.enabled_agenda_sync_switch);
        if (user.isEnabledAgendaSync()) {
            agenda_sync_switch.setChecked(true);
        }

        agenda_sync_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                user.setEnabledAgendaSync(isChecked);
                realm.commitTransaction();
            }
        });

        Switch alarm_sync_switch = (Switch) view.findViewById(R.id.enabled_alarm_sync_switch);
        if (user.isEnabledAlarmSync()) {
            alarm_sync_switch.setChecked(true);
        }

        alarm_sync_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                user.setEnabledAlarmSync(isChecked);
                realm.commitTransaction();
            }
        });

        return view;
    }
}
