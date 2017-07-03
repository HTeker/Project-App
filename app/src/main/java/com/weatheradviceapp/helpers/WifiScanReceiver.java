package com.weatheradviceapp.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.weatheradviceapp.models.User;

import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Halil Teker on 7/2/2017.
 */

public class WifiScanReceiver extends BroadcastReceiver {
    private User user;

    public WifiScanReceiver(){
        user = User.getOrCreateUser();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();

        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI){
            user.setLastConnectedWifi(info.getSSID());
        }
        else{
            RealmList networks = user.getWifiNetworks();
            RealmResults savedNetworks = networks.where().equalTo("name", user.getLastConnectedWifi()).findAll();

            if(savedNetworks.size() != 0){
                // TODO: Push notification when saved WiFi network is disconnected
                Log.d("WifiReceiver", "Push");
            }else{
                Log.d("WifiReceiver", "Not push");
            }
        }

    }
}