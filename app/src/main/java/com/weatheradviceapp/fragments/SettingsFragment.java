package com.weatheradviceapp.fragments;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;

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
import com.weatheradviceapp.models.Network;
import com.weatheradviceapp.models.User;
import com.weatheradviceapp.adapters.WhitelistedWifiAdapter;

import io.realm.Realm;

public class SettingsFragment extends Fragment {

    private Realm realm;
    private User user;
    MapView mMapView;
    LinearLayout container_whitelisted_wifi;
    private GoogleMap googleMap;
    private Marker marker;

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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

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

        realm.beginTransaction();
        user.addWifiNetwork(new Network("Netwerk 1"));
        user.addWifiNetwork(new Network("Netwerk 2"));
        user.addWifiNetwork(new Network("Netwerk 3"));
        realm.commitTransaction();

        ListAdapter wifiAdapter = new WhitelistedWifiAdapter(getActivity(), user.getWifiNetworks());
        ListView list_whitelisted_wifi = (ListView) view.findViewById(R.id.list_whitelisted_wifi_networks);
        list_whitelisted_wifi.setAdapter(wifiAdapter);

        Switch agenda_sync_switch = (Switch) view.findViewById(R.id.enabled_agenda_sync_switch);
        if (user.isEnabledAgendaSync()) {
            agenda_sync_switch.setChecked(true);
        }

        Button btn_add_wifi = (Button) view.findViewById(R.id.button_add_wifi);
        btn_add_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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
}
