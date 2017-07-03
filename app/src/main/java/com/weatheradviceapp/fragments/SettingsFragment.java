package com.weatheradviceapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.Manifest;

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

import com.robertlevonyan.views.chip.Chip;
import com.robertlevonyan.views.chip.OnSelectClickListener;
import com.weatheradviceapp.R;
import com.weatheradviceapp.helpers.AdviceFactory;
import com.weatheradviceapp.jobs.SyncCalendarJob;
import com.weatheradviceapp.models.ActivityAdvice;
import com.weatheradviceapp.models.Advice;
import com.weatheradviceapp.models.Interest;
import com.weatheradviceapp.helpers.WifiScanReceiver;
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

        final WhitelistedWifiAdapter wifiAdapter = new WhitelistedWifiAdapter(getActivity(), user.getWifiNetworks());
        ListView list_whitelisted_wifi = (ListView) container_whitelisted_wifi.findViewById(R.id.list_whitelisted_wifi_networks);
        list_whitelisted_wifi.setAdapter(wifiAdapter);
        setListViewHeightBasedOnChildren(list_whitelisted_wifi);


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

                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
                    builderSingle.setTitle(R.string.select_network);

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_singlechoice);

                    for(int i=1; i<configuredNetworks.size(); i++){
                        arrayAdapter.add(configuredNetworks.get(i).SSID);
                    }

                    builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String strName = arrayAdapter.getItem(which);
                            Network network = new Network(strName);

                            RealmList networks = user.getWifiNetworks();

                            RealmResults savedNetworks = networks.where().equalTo("name", strName).findAll();

                            if(savedNetworks.size() == 0){
                                realm.beginTransaction();
                                user.addWifiNetwork(network);
                                realm.commitTransaction();

                                wifiAdapter.notifyDataSetChanged();
                            }
                        }
                    });

                    builderSingle.show();
                }else{
                    final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle(R.string.wifi_is_disabled);
                    alertDialog.setMessage(getContext().getString(R.string.enable_wifi_try_again));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getContext().getString(R.string.ok),
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


        // Hier gaan we de activiteiten chips maken. Door dit te doen vanuit de ActivityAdvice classes
        // hoeven we niets in de layout hard te coden en kun je nieuwe toevoegen door alleen een nieuwe
        // subclass van ActivityAdvice te maken.
        List<Advice> activities = AdviceFactory.getAllAdviceInstances(AdviceFactory.Filter.ACTIVITY);

        RelativeLayout chips_parent = (RelativeLayout) view.findViewById(R.id.activities_selection);
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 5, 5);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        for(int i = 0; i < activities.size(); i++) {
            Advice advice = activities.get(i);
            if (advice instanceof ActivityAdvice) {
                ActivityAdvice activity = (ActivityAdvice)advice;

                Chip chip = new Chip(getContext());
                chip.setId(i+1); // ID mag geen 0 zijn!!!
                // We gebruiken de classname als identifier om op te slaan in de user. Deze classname
                // slaan we in de chip op in de Tag.
                chip.setTag(activity.getClass().getSimpleName());
                chip.setBackgroundResource(activity.getChipColorResource());
                chip.setChipText(getString(activity.getChipCaptionResource()));
                chip.setChipIcon(ContextCompat.getDrawable(getContext(), activity.getAdviceIconResource()));
                chip.setHasIcon(true);
                chip.setSelectable(true);
                chip.setSelected(activity.checkInterest()); // Werkt niet, chip reageert alleen op clicks
                chip.setOnSelectClickListener(new OnSelectClickListener() {
                    @Override
                    public void onSelectClick(View v, boolean selected) {

                        // First find the chip view because the view here is zomething else
                        View p = (View) v.getParent();

                        // We gaan kijken of de chip moet worden toegevoegd aan de user interests
                        // of juist verwijderd moet worden. Dat is de functie van de chips.
                        realm.beginTransaction();
                        RealmList<Interest> new_interests = new RealmList();
                        new_interests.addAll(user.getInterests());

                        String chipName = (String) p.getTag();
                        if (selected) {
                            Interest new_interest = realm.createObject(Interest.class);
                            new_interest.setName(chipName);
                            new_interests.add(new_interest);
                        } else {
                            for(int i = new_interests.size() - 1; i >= 0; i--) {
                                if (new_interests.get(i).getName().equals(chipName)) {
                                    new_interests.remove(i);
                                }
                            }
                        }
                        user.setInterests(new_interests);
                        realm.commitTransaction();
                    }
                });

                chip.setLayoutParams(layoutParams);
                chips_parent.addView(chip);

                // For the next chip create new params, and make alignment to the current chip
                layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, 5, 5);
                if ((i + 1) % 3 == 0) {
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    layoutParams.addRule(RelativeLayout.BELOW, chip.getId());
                } else {
                    layoutParams.addRule(RelativeLayout.END_OF, chip.getId());
                    layoutParams.addRule(RelativeLayout.ALIGN_TOP, chip.getId());
                }

            }
        }

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

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
