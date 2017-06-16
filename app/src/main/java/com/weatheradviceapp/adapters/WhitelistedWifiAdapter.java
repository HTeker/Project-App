package com.weatheradviceapp.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import com.weatheradviceapp.R;

class WhitelistedWifiAdapter extends ArrayAdapter<String> {
    public WhitelistedWifiAdapter(@NonNull Context context, String[] networks) {
        super(context, R.layout.whitelisted_wifi, networks);
    }
}
