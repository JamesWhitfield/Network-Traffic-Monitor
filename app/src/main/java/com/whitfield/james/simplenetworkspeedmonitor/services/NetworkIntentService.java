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


            try {
                while (!stop){


                    downCurrent = TrafficStats.getTotalRxBytes();
                    upCurrent = TrafficStats.getTotalTxBytes();

                    String output = "";
                    //Download output
                    if(down){
                        Long kbs = (downCurrent - downStart)/1024;
//                        kbs = Long.valueOf(3567);
                        if(kbs > 1024){
                            double x = (double)kbs/(double)1024;
                            double fin = (Math.round(x *100.0)/100.0);
                            if(tray && trayDown){
                                Log.i("TRAY","DOWN");
                                setSmallIconMb(builder, fin);
                            }
                            output = output + Html.fromHtml("\u25bc")+ fin + "/Mbs    ";
                        }else{
                            output = output + Html.fromHtml("\u25bc")+ kbs + "/kbs    ";
                            if(tray && trayDown) {
                                Log.i("TRAY","DOWN");
                                setSmallIconKb(builder, kbs);
                            }
                        }

                    }
                    //Upload Output
                    if(up){
                        Long kbs = (upCurrent - upStart)/1024;
//                        kbs = Long.valueOf(287);
                        if(kbs > 1024){
                            double x = (double)kbs/(double)1024;
                            double fin = (Math.round(x *100.0)/100.0);
                            if(tray && !trayDown){
                                Log.i("TRAY","UP");
                                setSmallIconMb(builder, fin);
                            }
                            output = output +  Html.fromHtml("\u25b2") + fin + "/Mbs";
                        }else{
                            output = output +  Html.fromHtml("\u25b2") + kbs + "/kbs";
                            if(tray && !trayDown){
                                Log.i("TRAY","UP");
                                setSmallIconKb(builder, kbs);
                            }
                        }
                    }

                    RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.custom_notification);
                    remoteViews.setTextViewText(R.id.tvContent, output);
                    Log.i("OUTPUT",output);
                    builder.setContent(remoteViews);



                    builder.setShowWhen(false);
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

    private void setSmallIconMb(NotificationCompat.Builder builder, double fin) {

        if(fin < 1.1){
            builder.setSmallIcon(R.drawable.ic_stat_1mb);
        }else if( fin < 1.2){
            builder.setSmallIcon(R.drawable.ic_stat_1_1mb);
        }else if( fin < 1.3){
            builder.setSmallIcon(R.drawable.ic_stat_1_2mb);
        }else if( fin < 1.4){
            builder.setSmallIcon(R.drawable.ic_stat_1_3mb);
        }else if( fin < 1.5){
            builder.setSmallIcon(R.drawable.ic_stat_1_4mb);
        }else if( fin < 1.6){
            builder.setSmallIcon(R.drawable.ic_stat_1_5mb);
        }else if( fin < 1.7){
            builder.setSmallIcon(R.drawable.ic_stat_1_6mb);
        }else if( fin < 1.8){
            builder.setSmallIcon(R.drawable.ic_stat_1_7mb);
        }else if( fin < 1.9){
            builder.setSmallIcon(R.drawable.ic_stat_1_8mb);
        }else if( fin < 2){
            builder.setSmallIcon(R.drawable.ic_stat_1_9mb);
        }else if( fin < 2.1){
            builder.setSmallIcon(R.drawable.ic_stat_2mb);
        }else if( fin < 2.2){
            builder.setSmallIcon(R.drawable.ic_stat_2_1mb);
        }else if( fin < 2.3){
            builder.setSmallIcon(R.drawable.ic_stat_2_2mb);
        }else if( fin < 2.4){
            builder.setSmallIcon(R.drawable.ic_stat_2_3mb);
        }else if( fin < 2.5){
            builder.setSmallIcon(R.drawable.ic_stat_2_4mb);
        }else if( fin < 2.6){
            builder.setSmallIcon(R.drawable.ic_stat_2_5mb);
        }else if( fin < 2.7){
            builder.setSmallIcon(R.drawable.ic_stat_2_6mb);
        }else if( fin < 2.8){
            builder.setSmallIcon(R.drawable.ic_stat_2_7mb);
        }else if( fin < 2.9){
            builder.setSmallIcon(R.drawable.ic_stat_2_8mb);
        }else if( fin < 3){
            builder.setSmallIcon(R.drawable.ic_stat_2_9mb);
        }else if( fin < 3.2){
            builder.setSmallIcon(R.drawable.ic_stat_3mb);
        }else if( fin < 3.4){
            builder.setSmallIcon(R.drawable.ic_stat_3_2mb);
        }else if( fin < 3.6){
            builder.setSmallIcon(R.drawable.ic_stat_3_4mb);
        }else if( fin < 3.8){
            builder.setSmallIcon(R.drawable.ic_stat_3_6mb);
        }else if( fin < 4){
            builder.setSmallIcon(R.drawable.ic_stat_3_8mb);
        }else if( fin < 4.2){
            builder.setSmallIcon(R.drawable.ic_stat_4mb);
        }else if( fin < 4.4){
            builder.setSmallIcon(R.drawable.ic_stat_4_2mb);
        }else if( fin < 4.6){
            builder.setSmallIcon(R.drawable.ic_stat_4_4mb);
        }else if( fin < 4.8){
            builder.setSmallIcon(R.drawable.ic_stat_4_6mb);
        }else if( fin < 5){
            builder.setSmallIcon(R.drawable.ic_stat_4_8mb);
        }else if( fin < 5.5){
            builder.setSmallIcon(R.drawable.ic_stat_5mb);
        }else if( fin < 6){
            builder.setSmallIcon(R.drawable.ic_stat_5_5mb);
        }else if( fin < 6.5){
            builder.setSmallIcon(R.drawable.ic_stat_6mb);
        }else if( fin < 7){
            builder.setSmallIcon(R.drawable.ic_stat_6_5mb);
        }else if( fin < 7.5){
            builder.setSmallIcon(R.drawable.ic_stat_7mb);
        }else if( fin < 8){
            builder.setSmallIcon(R.drawable.ic_stat_7_5mb);
        }else if( fin < 8.5){
            builder.setSmallIcon(R.drawable.ic_stat_8mb);
        }else if( fin < 9){
            builder.setSmallIcon(R.drawable.ic_stat_8_5mb);
        }else if( fin < 9.5){
            builder.setSmallIcon(R.drawable.ic_stat_9mb);
        }else if( fin < 10){
            builder.setSmallIcon(R.drawable.ic_stat_9_5mb);
        }else if( fin < 11){
            builder.setSmallIcon(R.drawable.ic_stat_10mb);
        }else if( fin < 12){
            builder.setSmallIcon(R.drawable.ic_stat_11mb);
        }else if( fin < 13){
            builder.setSmallIcon(R.drawable.ic_stat_12mb);
        }else if( fin < 14){
            builder.setSmallIcon(R.drawable.ic_stat_13mb);
        }else if( fin < 15){
            builder.setSmallIcon(R.drawable.ic_stat_14mb);
        }else if( fin < 16){
            builder.setSmallIcon(R.drawable.ic_stat_15mb);
        }else if( fin < 17){
            builder.setSmallIcon(R.drawable.ic_stat_16mb);
        }else if( fin < 18){
            builder.setSmallIcon(R.drawable.ic_stat_17mb);
        }else if( fin < 19){
            builder.setSmallIcon(R.drawable.ic_stat_18mb);
        }else if( fin < 20){
            builder.setSmallIcon(R.drawable.ic_stat_19mb);
        }else if( fin < 21){
            builder.setSmallIcon(R.drawable.ic_stat_20mb);
        }else if( fin < 22){
            builder.setSmallIcon(R.drawable.ic_stat_21mb);
        }else if( fin < 23){
            builder.setSmallIcon(R.drawable.ic_stat_22mb);
        }else if( fin < 24){
            builder.setSmallIcon(R.drawable.ic_stat_23mb);
        }else if( fin < 25){
            builder.setSmallIcon(R.drawable.ic_stat_24mb);
        }else if( fin < 26){
            builder.setSmallIcon(R.drawable.ic_stat_25mb);
        }else if( fin < 27){
            builder.setSmallIcon(R.drawable.ic_stat_26mb);
        }else if( fin < 28){
            builder.setSmallIcon(R.drawable.ic_stat_27mb);
        }else if( fin < 29){
            builder.setSmallIcon(R.drawable.ic_stat_28mb);
        }else if( fin < 30){
            builder.setSmallIcon(R.drawable.ic_stat_29mb);
        }else if( fin < 31){
            builder.setSmallIcon(R.drawable.ic_stat_30mb);
        }else if( fin < 32){
            builder.setSmallIcon(R.drawable.ic_stat_31mb);
        }else if( fin < 33){
            builder.setSmallIcon(R.drawable.ic_stat_32mb);
        }else if( fin < 34){
            builder.setSmallIcon(R.drawable.ic_stat_33mb);
        }else if( fin < 35){
            builder.setSmallIcon(R.drawable.ic_stat_34mb);
        }else if( fin < 36){
            builder.setSmallIcon(R.drawable.ic_stat_35mb);
        }else if( fin < 37){
            builder.setSmallIcon(R.drawable.ic_stat_36mb);
        }else if( fin < 38){
            builder.setSmallIcon(R.drawable.ic_stat_37mb);
        }else if( fin < 39){
            builder.setSmallIcon(R.drawable.ic_stat_38mb);
        }else if( fin < 40){
            builder.setSmallIcon(R.drawable.ic_stat_39mb);
        }else if( fin < 41){
            builder.setSmallIcon(R.drawable.ic_stat_40mb);
        }else if( fin < 42){
            builder.setSmallIcon(R.drawable.ic_stat_41mb);
        }else if( fin < 43){
            builder.setSmallIcon(R.drawable.ic_stat_42mb);
        }else if( fin < 44){
            builder.setSmallIcon(R.drawable.ic_stat_43mb);
        }else if( fin < 45){
            builder.setSmallIcon(R.drawable.ic_stat_44mb);
        }else if( fin < 46){
            builder.setSmallIcon(R.drawable.ic_stat_45mb);
        }else if( fin < 47){
            builder.setSmallIcon(R.drawable.ic_stat_46mb);
        }else if( fin < 48){
            builder.setSmallIcon(R.drawable.ic_stat_47mb);
        }else if( fin < 49){
            builder.setSmallIcon(R.drawable.ic_stat_48mb);
        }else if( fin < 50){
            builder.setSmallIcon(R.drawable.ic_stat_49mb);
        }else if( fin < 51){
            builder.setSmallIcon(R.drawable.ic_stat_50mb);
        }else if( fin >= 51){
            builder.setSmallIcon(R.drawable.ic_stat_50mb_big);
        }
    }

    public void setSmallIconKb(NotificationCompat.Builder builder, double fin){


        if(fin > 900){
            builder.setSmallIcon(R.drawable.ic_stat_0_9mb);
        }else if(fin > 800){
            builder.setSmallIcon(R.drawable.ic_stat_0_8mb);
        }else if(fin > 700){
            builder.setSmallIcon(R.drawable.ic_stat_0_7mb);
        }else if(fin > 600){
            builder.setSmallIcon(R.drawable.ic_stat_0_6mb);
        }else if(fin > 500){
            builder.setSmallIcon(R.drawable.ic_stat_0_5mb);
        }else if(fin > 400){
            builder.setSmallIcon(R.drawable.ic_stat_0_4mb);
        }else if(fin > 300){
            builder.setSmallIcon(R.drawable.ic_stat_0_3mb);
        }else if(fin > 200){
            builder.setSmallIcon(R.drawable.ic_stat_0_2mb);
        }else if(fin > 100){
            builder.setSmallIcon(R.drawable.ic_stat_0_1mb);
        }else if(fin > 90){
            builder.setSmallIcon(R.drawable.ic_stat_90kb);
        }else if(fin > 80){
            builder.setSmallIcon(R.drawable.ic_stat_80kb);
        }else if(fin > 70){
            builder.setSmallIcon(R.drawable.ic_stat_70kb);
        }else if(fin > 60){
            builder.setSmallIcon(R.drawable.ic_stat_60kb);
        }else if(fin > 50){
            builder.setSmallIcon(R.drawable.ic_stat_50kb);
        }else if(fin > 45){
            builder.setSmallIcon(R.drawable.ic_stat_45kb);
        }else if(fin > 40){
            builder.setSmallIcon(R.drawable.ic_stat_40kb);
        }else if(fin > 35){
            builder.setSmallIcon(R.drawable.ic_stat_35kb);
        }else if(fin > 30){
            builder.setSmallIcon(R.drawable.ic_stat_30kb);
        }else if(fin > 25){
            builder.setSmallIcon(R.drawable.ic_stat_25kb);
        }else if(fin > 20){
            builder.setSmallIcon(R.drawable.ic_stat_20kb);
        }else if(fin > 15){
            builder.setSmallIcon(R.drawable.ic_stat_15kb);
        }else if(fin > 10){
            builder.setSmallIcon(R.drawable.ic_stat_10kb);
        }else if(fin > 10){
            builder.setSmallIcon(R.drawable.ic_stat_10kb);
        }else if(fin >= 9){
            builder.setSmallIcon(R.drawable.ic_stat_9kb);
        }else if(fin >= 8){
            builder.setSmallIcon(R.drawable.ic_stat_8kb);
        }else if(fin >= 7){
            builder.setSmallIcon(R.drawable.ic_stat_7kb);
        }else if(fin >= 6){
            builder.setSmallIcon(R.drawable.ic_stat_6kb);
        }else if(fin >= 5){
            builder.setSmallIcon(R.drawable.ic_stat_5kb);
        }else if(fin >= 4){
            builder.setSmallIcon(R.drawable.ic_stat_4kb);
        }else if(fin >= 3){
            builder.setSmallIcon(R.drawable.ic_stat_3kb);
        }else if(fin >= 2){
            builder.setSmallIcon(R.drawable.ic_stat_2kb);
        }else if(fin >= 1){
            builder.setSmallIcon(R.drawable.ic_stat_1kb);
        }else if(fin <= 1){
            builder.setSmallIcon(R.drawable.ic_stat_0kb);
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
