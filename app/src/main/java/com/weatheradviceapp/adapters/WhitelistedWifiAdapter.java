package com.weatheradviceapp.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Network;

import io.realm.RealmList;

public class WhitelistedWifiAdapter extends ArrayAdapter<Network> {
    public WhitelistedWifiAdapter(@NonNull Context context, RealmList<Network> networks) {
        super(context, R.layout.whitelisted_wifi, networks);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View whitelistedWifiView = inflater.inflate(R.layout.whitelisted_wifi, parent, false);

        Network singleNetwork = getItem(position);
        TextView txt = (TextView) whitelistedWifiView.findViewById(R.id.wifi_list_item);
        txt.setText(singleNetwork.getName());

        return whitelistedWifiView;
    }
}
