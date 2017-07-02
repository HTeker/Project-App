package com.weatheradviceapp.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ListAdapter;
import android.widget.Switch;

import com.evernote.android.job.JobRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.widget.TextView;

import com.weatheradviceapp.R;
import com.weatheradviceapp.helpers.WifiScanReceiver;
import com.weatheradviceapp.jobs.SyncCalendarJob;
import com.weatheradviceapp.models.Network;
import com.weatheradviceapp.models.User;
import com.weatheradviceapp.models.UserCalendar;
import com.weatheradviceapp.adapters.WhitelistedWifiAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class SettingsFragment extends Fragment {
    private Realm realm;
    private User user;
    MapView mMapView;
    LinearLayout container_whitelisted_wifi;
    private GoogleMap googleMap;
    private Marker marker;
    private View view;
    WifiManager mainWifiObj;

    SettingsFragmentAgendaListAdapter agendaListAdapter = null;

    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a Realm instance for this thread
        realm = Realm.getDefaultInstance();

        // Get user or create user if we don't have one yet.
        user = User.getOrCreateUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        Switch gps_switch = (Switch) view.findViewById(R.id.enabled_gps_location_switch);
        if (user.isEnabledGPSLocation()) {
            gps_switch.setChecked(true);
        } else {
            mMapView.setVisibility(LinearLayout.VISIBLE);
        }

        gps_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                user.setEnabledGPSLocation(isChecked);
                realm.commitTransaction();

                if (isChecked) {
                    mMapView.setVisibility(LinearLayout.GONE);
                } else {
                    mMapView.setVisibility(LinearLayout.VISIBLE);
                }
            }
        });

        Switch wifi_switch = (Switch) view.findViewById(R.id.enabled_wifi_location_switch);
        container_whitelisted_wifi = (LinearLayout) view.findViewById(R.id.container_whitelisted_wifi);
        if (user.isEnabledWiFiLocation()) {
            wifi_switch.setChecked(true);
            container_whitelisted_wifi.setVisibility(View.VISIBLE);
        }

        wifi_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                user.setEnabledWiFiLocation(isChecked);
                realm.commitTransaction();

                if (isChecked) {
                    container_whitelisted_wifi.setVisibility(View.VISIBLE);
                } else {
                    container_whitelisted_wifi.setVisibility(View.GONE);
                }
            }
        });

        if(user.getWifiNetworks().size() == 0){
            realm.beginTransaction();
            user.addWifiNetwork(new Network("Netwerk 1"));
            user.addWifiNetwork(new Network("Netwerk 2"));
            user.addWifiNetwork(new Network("Netwerk 3"));
            realm.commitTransaction();
        }

        ListAdapter wifiAdapter = new WhitelistedWifiAdapter(getActivity(), user.getWifiNetworks());
        ListView list_whitelisted_wifi = (ListView) container_whitelisted_wifi.findViewById(R.id.list_whitelisted_wifi_networks);
        list_whitelisted_wifi.setAdapter(wifiAdapter);

        Switch agenda_sync_switch = (Switch) view.findViewById(R.id.enabled_agenda_sync_switch);
        if (user.isEnabledAgendaSync()) {
            agenda_sync_switch.setChecked(true);
            fillUserAgendas(view, UserCalendar.getUserCalendars());
        }

        Button btn_add_wifi = (Button) view.findViewById(R.id.button_add_wifi);
        btn_add_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wifi = (WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifi.isWifiEnabled()){
                    //wifi is enabled
                    mainWifiObj = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    WifiScanReceiver wifiReciever = new WifiScanReceiver();
                    getActivity().getApplicationContext().registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                    List<WifiConfiguration> configuredNetworks = mainWifiObj.getConfiguredNetworks();

                    for(int i=1; i<configuredNetworks.size(); i++){
                        Log.d("Halil Wifi", configuredNetworks.get(i).toString());
                        Log.d("Halil Wifi", "----------------------------------");
                    }
                }else{
                    final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("WiFi staat uit");
                    alertDialog.setMessage("Zet uw WiFi aan en probeer het opnieuw");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        agenda_sync_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                user.setEnabledAgendaSync(isChecked);
                realm.commitTransaction();

                if (isChecked) {
                    fillUserAgendas(view, UserCalendar.getUserCalendars());
                } else {
                    ListView listView = (ListView) view.findViewById(R.id.agenda_select_list);
                    listView.setVisibility(View.GONE);
                }
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

        Switch demo_mode = (Switch) view.findViewById(R.id.enabled_demo_mode);
        if (user.isEnabledDemoMode()) {
            demo_mode.setChecked(true);
        }

        demo_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                user.setEnabledDemoMode(isChecked);
                realm.commitTransaction();
            }
        });

        boolean locationEnabled = false;
        if (!canAccessLocation()) {
            requestPermissions(LOCATION_PERMS, 1);
        } else {
            locationEnabled = true;
        }

        final boolean googleMapsGPS = locationEnabled;

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final SettingsFragment me = this;

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                try {
                    googleMap.setMyLocationEnabled(googleMapsGPS);
                } catch(Exception e) {

                }

                final MarkerOptions markerOptions = new MarkerOptions().draggable(true);
                LatLng currentPosition = new LatLng(0, 0);

                if (user.isSetOwnPosition()) {
                    currentPosition = new LatLng(user.getCustomLocationLat(), user.getCustomLocationLng());
                } else {
                    if (googleMapsGPS && googleMap.isMyLocationEnabled() && googleMap.getMyLocation() != null) {
                        currentPosition = new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude());
                    }
                }

                markerOptions.position(currentPosition);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(currentPosition).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location my_location) {
                        if (!user.isSetOwnPosition()) {
                            LatLng currentPosition = new LatLng(my_location.getLatitude(), my_location.getLongitude());
                            marker.setPosition(currentPosition);
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(currentPosition).zoom(12).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
                });

                googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker arg0) {

                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onMarkerDragEnd(Marker arg0) {
                        realm.beginTransaction();
                        user.setSetOwnPosition(true);
                        user.setCustomLocationLat(arg0.getPosition().latitude);
                        user.setCustomLocationLng(arg0.getPosition().longitude);
                        realm.commitTransaction();
                    }

                    @Override
                    public void onMarkerDrag(Marker arg0) {

                    }
                });

                marker = googleMap.addMarker(markerOptions);
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                marker.setPosition(new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude()));
                googleMap.setMyLocationEnabled(true);
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(getContext(), perm));
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void fillUserAgendas(View view, Cursor cur) {
        RealmList<UserCalendar> user_enabled_agendas = user.getAgendas();
        HashMap<Long, Boolean> hmap = new HashMap<Long, Boolean>();

        for (int i = 0; i < user_enabled_agendas.size(); i++) {
            hmap.put(user_enabled_agendas.get(i).getCalID(), true);
        }

        ArrayList<AvailableCalendar> agendaList = new ArrayList<AvailableCalendar>();

        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;

            // Get the field values
            calID = cur.getLong(UserCalendar.CALENDAR_PROJECTION_ID_INDEX);
            displayName = cur.getString(UserCalendar.CALENDAR_PROJECTION_DISPLAY_NAME_INDEX);

            AvailableCalendar calendar = new AvailableCalendar(calID, displayName, hmap.containsKey(calID));
            agendaList.add(calendar);
        }

        agendaListAdapter = new SettingsFragmentAgendaListAdapter(this.getContext(), R.layout.fragment_settings_agenda, agendaList);
        ListView listView = (ListView) view.findViewById(R.id.agenda_select_list);
        listView.setAdapter(agendaListAdapter);
        listView.setVisibility(View.VISIBLE);
    }

    private class SettingsFragmentAgendaListAdapter extends ArrayAdapter<AvailableCalendar> {

        private ArrayList<AvailableCalendar> calendarList;

        public SettingsFragmentAgendaListAdapter(Context context, int textViewResourceId,
                               ArrayList<AvailableCalendar> calendarList) {
            super(context, textViewResourceId, calendarList);
            this.calendarList = new ArrayList<AvailableCalendar>();
            this.calendarList.addAll(calendarList);
        }

        private class ViewHolder {
            Switch name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.fragment_settings_agenda, null);

                holder = new ViewHolder();
                holder.name = (Switch) convertView.findViewById(R.id.agenda_select_list_option);
                convertView.setTag(holder);

                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        RealmList<UserCalendar> user_enabled_agendas = user.getAgendas();
                        HashMap<Long, Integer> hmap = new HashMap<Long, Integer>();

                        for (int i = 0; i < user_enabled_agendas.size(); i++) {
                            hmap.put(user_enabled_agendas.get(i).getCalID(), i);
                        }

                        Switch cb = (Switch) v;
                        AvailableCalendar calendar = (AvailableCalendar) cb.getTag();

                        if (cb.isChecked() && !hmap.containsKey(calendar.getCalID())) {
                            // If is checked and not in map yet, add it to list.
                            realm.beginTransaction();
                            RealmList<UserCalendar> userCaledendarslist = new RealmList();
                            userCaledendarslist.addAll(user_enabled_agendas);
                            UserCalendar new_calendar = realm.createObject(UserCalendar.class);
                            new_calendar.setCalID(calendar.getCalID());
                            new_calendar.setDisplayName(calendar.getDisplayName());
                            userCaledendarslist.add(new_calendar);
                            user.setAgendas(userCaledendarslist);
                            realm.commitTransaction();
                        }
                        else if(!cb.isChecked() && hmap.containsKey(calendar.getCalID())) {
                            // If is not checked and is in map, remove from list.
                            realm.beginTransaction();
                            RealmList<UserCalendar> userCaledendarslist = new RealmList();
                            userCaledendarslist.addAll(user_enabled_agendas);
                            int calendar_index = hmap.get(calendar.getCalID());
                            userCaledendarslist.remove(calendar_index);
                            user.setAgendas(userCaledendarslist);
                            realm.commitTransaction();
                        }

                        calendar.setSelected(cb.isChecked());
                        fetchCalendarWeather();
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            AvailableCalendar calendar = calendarList.get(position);
            holder.name.setText(calendar.getDisplayName());
            holder.name.setChecked(calendar.isSelected());
            holder.name.setTag(calendar);

            return convertView;
        }
    }

    private class AvailableCalendar {
        long calID;
        String displayName;
        boolean selected = false;

        public AvailableCalendar(long calID, String displayName, boolean selected) {
            super();
            this.calID = calID;
            this.displayName = displayName;
            this.selected = selected;
        }

        public long getCalID() {
            return calID;
        }

        public void setCalID(long calID) {
            this.calID = calID;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    private void fetchCalendarWeather() {
        new JobRequest.Builder(SyncCalendarJob.TAG)
                .setExecutionWindow(3_000L, 4_000L)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.LINEAR)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setPersisted(true)
                .build()
                .schedule();
    }
}
