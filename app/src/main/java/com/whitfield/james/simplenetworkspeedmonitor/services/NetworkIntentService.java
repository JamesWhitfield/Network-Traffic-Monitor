package com.whitfield.james.simplenetworkspeedmonitor.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.home.HomeActivity;


/**
 * Created by jwhit on 18/01/2016.
 */
public class NetworkIntentService extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private boolean stop  = false;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    private static final int NOTIFICATION_ID = 10000;
    public static final String NAME = "NetworkService";
    Boolean up,down,lockScreen = null;
    private Intent intent;


    @Override
    public void onDestroy() {
        super.onDestroy();

        stop = true;
        mServiceHandler = null;
        Log.i(NAME, "Stop");
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            Log.i(NAME, "Start");

            Bundle bundle = intent.getExtras();

            if(bundle.containsKey(HomeActivity.INTENT_TAG_DOWN)){
                down = bundle.getBoolean(HomeActivity.INTENT_TAG_DOWN);
            }
            if(bundle.containsKey(HomeActivity.INTENT_TAG_UP)){
                up = bundle.getBoolean(HomeActivity.INTENT_TAG_UP);
            }
            if(bundle.containsKey(HomeActivity.INTENT_TAG_LOCK_SCREEN)){
                lockScreen = bundle.getBoolean(HomeActivity.INTENT_TAG_LOCK_SCREEN);
            }

            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("Network speed")
//                    .setContentText("Setup...")
                    .setSmallIcon(R.drawable.ic_stat_)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    ;

            if(lockScreen == true){
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            }else{
                builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
            }

            startForeground(NOTIFICATION_ID, builder.build());


            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Long downStart = TrafficStats.getTotalRxBytes();
            Long upStart = TrafficStats.getTotalTxBytes();
            Long downCurrent;
            Long upCurrent;


            try {
                while (!stop){


                    downCurrent = TrafficStats.getTotalRxBytes();
                    upCurrent = TrafficStats.getTotalTxBytes();

                    String output = "";
                    if(down){
                        Long kbs = (downCurrent - downStart)/1024;
                        kbs = Long.valueOf(3567);
                        if(kbs > 1024){
                            double x = (double)kbs/(double)1024;
                            output = output + Html.fromHtml("\u25bc")+ (Math.round(x *100.0)/100.0) + "/Mbs    ";
                        }else{
                            output = output + Html.fromHtml("\u25bc")+ kbs + "/kbs    ";
                        }
                    }
                    if(up){
                        Long kbs = (upCurrent - upStart)/1024;
                        kbs = Long.valueOf(287);                        if(kbs > 1024){
                            double x = (double)kbs/(double)1024;
                            output = output +  Html.fromHtml("\u25b2") + (Math.round(x *100.0)/100.0) + "/Mbs";
                        }else{
                            output = output +  Html.fromHtml("\u25b2") + kbs + "/kbs";
                        }
                    }


                    builder.setContentTitle(output)
                            .setShowWhen(false);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());

                    downStart = downCurrent;
                    upStart = upCurrent;


                    Thread.sleep(1*1000);

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }
}
