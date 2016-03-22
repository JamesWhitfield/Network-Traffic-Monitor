package com.whitfield.james.simplenetworkspeedmonitor.home;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.application.ApplicationController;
import com.whitfield.james.simplenetworkspeedmonitor.services.NetworkIntentService;

/**
 * Created by jwhit on 18/01/2016.
 */
public class HomeFragment extends Fragment {

    private HomeActivityInterface homeActivityInterface;

    private Button btnStart;
    private CheckBox cbUp,cbdown,cbRestart,cbLockScreen,cbTray,cbSplit;
    private RadioGroup rgTray;
    private RadioButton rbDownload,rbUpload;
    private Boolean trayDown;
    private Tracker tracker;
    private ActionBar actionBar;


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

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle("Live Monitor");

        view = inflater.inflate(R.layout.home_screen,container,false);


        btnStart = (Button) view.findViewById(R.id.btnStart);
        cbdown = (CheckBox) view.findViewById(R.id.cbDown);
        cbUp = (CheckBox) view.findViewById(R.id.cbUpload);
        cbRestart = (CheckBox) view.findViewById(R.id.cbRestart);
        cbLockScreen = (CheckBox) view.findViewById(R.id.cbLockScreen);
        cbTray = (CheckBox) view.findViewById(R.id.cbTray);
        cbSplit = (CheckBox) view.findViewById(R.id.cbSplitMobileWifi);

        rgTray = (RadioGroup) view.findViewById(R.id.rgTray);
        rbDownload = (RadioButton) view.findViewById(R.id.rbDownload);
        rbUpload = (RadioButton) view.findViewById(R.id.rbUpload);


        setViewValues();
        return view;
    }

    public void setViewValues(){

        setupSettings();
        startServiceSetup();

        test();
    }

    private void test() {

        //TODO max speed

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivity = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] networks = connectivity.getAllNetworks();
            for (int i = 0; i < networks.length; i++) {
                NetworkCapabilities capabilities = connectivity.getNetworkCapabilities(networks[i]);
                Log.i("Net",capabilities.toString());
                Log.i("Net", String.valueOf(capabilities.getLinkDownstreamBandwidthKbps()));
                Log.i("Net", String.valueOf(capabilities.getLinkUpstreamBandwidthKbps()));
            }
        }



    }

    private void setupSettings() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.preferences_key), getActivity().MODE_PRIVATE);

        cbUp.setChecked(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_UP, true));
        cbdown.setChecked(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_DOWN, true));
        cbRestart.setChecked(sharedPreferences.getBoolean(getString(R.string.restart_key), true));
        cbLockScreen.setChecked(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_LOCK_SCREEN,true));
        cbTray.setChecked(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_TRAY,true));
        cbSplit.setChecked(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_SPLIT,false));

        if(sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_TRAY_DOWN,true)){

            rbDownload.setChecked(true);
        }else{
            rbUpload.setChecked(true);
        }


        if(cbTray.isChecked()){
            rbUpload.setEnabled(true);
            rbDownload.setEnabled(true);
        }else{
            rbUpload.setEnabled(false);
            rbDownload.setEnabled(false);
        }

        cbTray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cbTray.isChecked()){
                    rbUpload.setEnabled(true);
                    rbDownload.setEnabled(true);
                }else{
                    rbUpload.setEnabled(false);
                    rbDownload.setEnabled(false);
                }
            }
        });
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

                    if(rbDownload.isChecked()){
                        trayDown = true;
                    }else{
                        trayDown = false;
                    }
                    //Start
                    homeActivityInterface.startService(cbUp.isChecked(), cbdown.isChecked(), cbRestart.isChecked(), cbLockScreen.isChecked(),cbTray.isChecked(),trayDown,cbSplit.isChecked());
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
