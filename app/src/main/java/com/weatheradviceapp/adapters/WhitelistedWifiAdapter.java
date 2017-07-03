package com.weatheradviceapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weatheradviceapp.R;
import com.weatheradviceapp.models.Network;
import com.weatheradviceapp.models.User;

import io.realm.Realm;
import io.realm.RealmList;

public class WhitelistedWifiAdapter extends ArrayAdapter<Network> {

    private Realm realm;
    private User user;
    private WhitelistedWifiAdapter self;

    public WhitelistedWifiAdapter(@NonNull Context context, RealmList<Network> networks) {
        super(context, R.layout.whitelisted_wifi, networks);
        this.self = this;

        // Get a Realm instance for this thread
        realm = Realm.getDefaultInstance();

        // Get user or create user if we don't have one yet.
        user = User.getOrCreateUser();
    }

    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View whitelistedWifiView = inflater.inflate(R.layout.whitelisted_wifi, parent, false);

        final Network singleNetwork = getItem(position);
        TextView txt = (TextView) whitelistedWifiView.findViewById(R.id.wifi_list_item);
        txt.setText(singleNetwork.getName());
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle(R.string.delete_wifi);
                alertDialog.setMessage(getContext().getString(R.string.delete_wifi_confirm));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getContext().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getContext().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                realm.beginTransaction();
                                user.removeWifiNetwork(singleNetwork);
                                realm.commitTransaction();

                                self.notifyDataSetChanged();
                            }
                        });
                alertDialog.show();
            }
        });

        return whitelistedWifiView;
    }
}
