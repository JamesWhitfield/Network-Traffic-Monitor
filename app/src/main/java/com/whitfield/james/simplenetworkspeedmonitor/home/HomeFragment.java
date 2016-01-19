package com.whitfield.james.simplenetworkspeedmonitor.home;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.application.ApplicationController;
import com.whitfield.james.simplenetworkspeedmonitor.services.NetworkIntentService;

/**
 * Created by jwhit on 18/01/2016.
 */
public class HomeFragment extends Fragment {

    HomeActivityInterface homeActivityInterface;
    Boolean running;

    Button btnStart;
    CheckBox cbUp,cbdown,cbRestart,cbLockScreen;

    Tracker tracker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeActivityInterface = ((HomeActivityInterface) getActivity());
        tracker = ((ApplicationController)getActivity().getApplication()).getDefaultTracker();

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;

        view = inflater.inflate(R.layout.home_screen,container,false);


        btnStart = (Button) view.findViewById(R.id.btnStart);
        cbdown = (CheckBox) view.findViewById(R.id.cbDown);
        cbUp = (CheckBox) view.findViewById(R.id.cbUpload);
        cbRestart = (CheckBox) view.findViewById(R.id.cbRestart);
        cbLockScreen = (CheckBox) view.findViewById(R.id.cbLockScreen);


        setViewValues();
        return view;
    }

    public void setViewValues(){

        setupSettings();
            startServiceSetup();

    }

    private void setupSettings() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.preferences_key), getActivity().MODE_PRIVATE);
        cbUp.setChecked(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_UP, true));
        cbdown.setChecked(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_DOWN, true));
        cbRestart.setChecked(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_LOCK_SCREEN, true));
        cbLockScreen.setChecked(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_LOCK_SCREEN,true));

    }

    public void startServiceSetup(){
        if(isMyServiceRunning(NetworkIntentService.class)){
            btnStart.setText("Stop");
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //STOP
                    homeActivityInterface.stopService();
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Action")
                            .setAction("Stop click")
                            .build());
                    startServiceSetup();

                }
            });
        }else{
            btnStart.setText("Start");
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Start
                    homeActivityInterface.startService(cbUp.isChecked(), cbdown.isChecked(), cbRestart.isChecked(), cbLockScreen.isChecked());
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Action")
                            .setAction("Start click")
                            .build());
                    startServiceSetup();
                    sendStartStats();
                }
            });
        }

    }

    private void sendStartStats() {

        //Send tracking events
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Stats")
                .setAction("Download")
                .setLabel(String.valueOf(cbdown.isChecked()))
                .build());
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Stats")
                .setAction("Upload")
                .setLabel(String.valueOf(cbUp.isChecked()))
                .build());
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Stats")
                .setAction("Restart")
                .setLabel(String.valueOf(cbRestart.isChecked()))
                .build());
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Stats")
                .setAction("Lock Screen")
                .setLabel(String.valueOf(cbLockScreen.isChecked()))
                .build());
    }

    @Override
    public void onResume() {
        super.onResume();

    }


}
