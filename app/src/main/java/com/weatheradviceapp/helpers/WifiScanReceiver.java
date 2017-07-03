package com.weatheradviceapp.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Halil Teker on 7/2/2017.
 */

public class WifiScanReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();

        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI){
            Log.d("WifiReceiver", "Have Wifi Connection");
        }
        else{
            Log.d("WifiReceiver", "Don't have Wifi Connection");
            // TODO: Push notification when saved WiFi network is disconnected
        }

    }
}