package com.whitfield.james.simplenetworkspeedmonitor.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.home.HomeActivity;

import java.util.Date;


/**
 * Created by jwhit on 18/01/2016.
 */
public class NetworkIntentService extends Service {

    private final static String TAG = "SpeedService";

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
    Boolean up,down,lockScreen,tray,trayDown = null;
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
            if(bundle.containsKey(HomeActivity.INTENT_TAG_TRAY)){
                tray = bundle.getBoolean(HomeActivity.INTENT_TAG_TRAY);
            }else{
                tray = false;
            }
            if(bundle.containsKey(HomeActivity.INTENT_TAG_TRAY_DOWN)){
                trayDown = bundle.getBoolean(HomeActivity.INTENT_TAG_TRAY_DOWN);
            }else{
                trayDown = false;
            }

            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("Network speed")
//                    .setContentText("Setup...")
                    .setSmallIcon(R.drawable.ic_stat_)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_STATUS);




            if(lockScreen == true){
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            }else{
                builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
            }


            Intent intent = new Intent(getBaseContext(),HomeActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(HomeActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent);


            startForeground(NOTIFICATION_ID, builder.build());


            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Long downStart = TrafficStats.getTotalRxBytes();
            Long upStart = TrafficStats.getTotalTxBytes();
            Long downCurrent;
            Long upCurrent;
            Long currentUpdate,diff;

            Long lastUpdate = new Date().getTime();


            while (!stop){

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {


                }
                if(!stop) {
                    downCurrent = TrafficStats.getTotalRxBytes();
                    upCurrent = TrafficStats.getTotalTxBytes();
                    currentUpdate = new Date().getTime();
                    diff = (currentUpdate - lastUpdate) / 1000;
                    if (diff == 0) {
                        Log.i(TAG, "wait...");
                        continue;
                    } else if (diff == 1) {
                        Log.i(TAG, "Update");

                    } else {
                        Log.i(TAG, "Late update");

                    }

                    update(downCurrent, downStart, upCurrent, upStart, builder, notificationManager);
                    //Prepare for next iteration
                    lastUpdate = currentUpdate;
                    downStart = downCurrent;
                    upStart = upCurrent;

                }



            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    private void update(Long downCurrent, Long downStart, Long upCurrent, Long upStart, NotificationCompat.Builder builder, NotificationManager notificationManager){

        String output = "";
        //Download output
        if(down){
            Long kbs = (downCurrent - downStart)/1024;
            if(kbs > 1024){
                long mbs = kbs/1024;
                output = output + Html.fromHtml("\u25bc")+ mbs + "/Mbs    ";
            }else{

                output = output + Html.fromHtml("\u25bc")+ kbs + "/Kbs    ";
            }

            if(tray && trayDown) {
                Log.i("TRAY","DOWN");
                setSmallIcon(builder, kbs);
            }
//                        }

        }
        //Upload Output
        if(up){
            Long kbs = (upCurrent - upStart)/1024;

            if(kbs > 1024){

                long mbs = kbs/1024;
                output = output +  Html.fromHtml("\u25b2") + mbs + "/Mbs";
            }else{
                output = output +  Html.fromHtml("\u25b2") + kbs + "/kbs";
            }

            if(tray && !trayDown){
                Log.i("TRAY","UP");
                setSmallIcon(builder, kbs);
            }
//                        }
        }

        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.custom_notification);
        remoteViews.setTextViewText(R.id.tvContent, output);
        Log.i("OUTPUT", output.trim());
        builder.setContent(remoteViews);

        builder.setShowWhen(false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }


    public void setSmallIcon(NotificationCompat.Builder builder, double fin){

        Long kbs = Math.round(fin);
        int i;

        if(kbs == 0.0){

            i = R.drawable.ic_stat_;

        }else if(kbs <= 99) {

            i = getResources().getIdentifier("ic_stat_" + Math.round(fin) + "k", "drawable", getPackageName());

        }else if (kbs <= 9949){

            String fileName = conevterKbsToDecimalfileNameString(fin);

            i = getResources().getIdentifier("ic_stat_" + fileName + "m", "drawable", getPackageName());
//            i = getResources().getIdentifier("ic_stat_" + "0_1" + "m", "drawable", getPackageName());

        }else if (kbs <=  50000){

            String fileName = conevterKbsToIntegerfileNameString(fin);
            i = getResources().getIdentifier("ic_stat_" + fileName + "m", "drawable", getPackageName());

        }else{

            i = R.drawable.ic_stat_50_plus_m;
        }

        builder.setSmallIcon(i);



    }
    private String conevterKbsToDecimalfileNameString(Double fin){

        Long j = Math.round(fin/100);
        Double l = j /10.0;
        String k = String.valueOf(l);
        return k.replace(".","_");
    }

    private String conevterKbsToIntegerfileNameString(Double fin){

        Long j = Math.round(fin/1000);
        String k = String.valueOf(j);
        return k;
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
