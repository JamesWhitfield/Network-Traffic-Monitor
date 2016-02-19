package com.whitfield.james.simplenetworkspeedmonitor.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.tools.ToolsFragment;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jwhit on 19/02/2016.
 */
public class MobileDataMonitorService extends Service {

    private Timer timer;
    int i;
    private static BroadcastReceiver networkReceiver;


    public MobileDataMonitorService() {
        super();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
        networkReceiver = null;
        stopTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Mobile","Service started");
        Bundle bundle = intent.getExtras();
        i = bundle.getInt(ToolsFragment.INTENT_SERVICE_TIME_TAG,10);
        Log.i("MObile","Wait period selected: " + i + " min");
        if(isMobileEnabled()) {
            startTimer();
        }
        registerNetworkReceiver();
        return START_REDELIVER_INTENT;
    }

    private void startTimer() {



        if(timer == null) {
            Log.i("Mobile","Starting timer...");
            timer = new Timer();
            timer.schedule(new MobileDataCheckTimerTask(),1000*60*i);
//            timer.schedule(new MobileDataCheckTimerTask(), 0);
        }else{
            Log.i("Mobile","Timer Already started");
        }

    }

    private void stopTimer(){

        if(timer != null){
            Log.i("Mobile","Stopping timer");
            timer.cancel();
            timer = null;
        }else{
            Log.i("Mobile","No timer to stop");
        }
    }

    private void registerNetworkReceiver()
    {
        networkReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {

                Log.i("Mobile", "Network change detected...");
                if(isMobileEnabled()){

                    Log.i("Mobile", "Mobile enabled");
                    startTimer();
                }else{
                    Log.i("Mobile", "Mobile disabled");
                    stopTimer();
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    private Boolean isMobileEnabled(){

        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean)method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        return mobileDataEnabled;
    }


    class MobileDataCheckTimerTask extends TimerTask {

        @Override
        public void run() {


            Log.i("Mobile", "Building notification");
            String msg = "Mobile data has been active for over " + i + " minutes";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

            builder.setSmallIcon(R.drawable.ic_warning_white_24dp);
//            builder.setLargeIcon(R.drawable.ic_warning_black_24dp);
            builder.setContentTitle("Mobile Data warning");
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
            builder.setContentText(msg);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(2, builder.build());

        }
    }
}
