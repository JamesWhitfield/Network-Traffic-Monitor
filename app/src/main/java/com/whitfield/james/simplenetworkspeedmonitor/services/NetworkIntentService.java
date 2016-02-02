package com.whitfield.james.simplenetworkspeedmonitor.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.home.HomeActivity;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by jwhit on 02/02/2016.
 */
public class NetworkIntentService extends Service {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    private final static String TAG = "SpeedServiceTest";

    private static final int NOTIFICATION_ID = 10000;
    public static final String NAME = "NetworkService";
    Boolean up,down,lockScreen,tray,trayDown = null;
    Timer timer;
    private Intent intent;

    public NetworkIntentService() {
        super();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        this.intent = intent;
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        startService();
        return START_REDELIVER_INTENT;
    }

    private void startService(){

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

        final NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
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

        Intent intent1= new Intent(getBaseContext(),HomeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(intent1);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);


        startForeground(NOTIFICATION_ID, builder.build());



        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Long downStart = TrafficStats.getTotalRxBytes(),
                    upStart = TrafficStats.getTotalTxBytes();
            Long downCurrent,
                    upCurrent;


            @Override
            public void run() {

                downCurrent = TrafficStats.getTotalRxBytes();
                upCurrent = TrafficStats.getTotalTxBytes();


                update(downCurrent, downStart, upCurrent, upStart, builder, notificationManager);
                //Prepare for next iteration
                downStart = downCurrent;
                upStart = upCurrent;
            }
        }, 0, 1000);

    }

    private void update(Long downCurrent, Long downStart, Long upCurrent, Long upStart, NotificationCompat.Builder builder, NotificationManager notificationManager){

        String output = "";
        //Download output

        Long kbs = (downCurrent - downStart)/1024;
        Long UpKbs = (upCurrent - upStart)/1024;

        if(down){
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


            if(UpKbs > 1024){
                long mbs = UpKbs/1024;
                output = output +  Html.fromHtml("\u25b2") + mbs + "/Mbs";
            }else{
                output = output +  Html.fromHtml("\u25b2") + UpKbs + "/kbs";
            }

            if(tray && !trayDown){
                Log.i("TRAY","UP");
                setSmallIcon(builder, UpKbs);
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

            String fileName = converterKbsToDecimalfileNameString(fin);

            i = getResources().getIdentifier("ic_stat_" + fileName + "m", "drawable", getPackageName());
//            i = getResources().getIdentifier("ic_stat_" + "0_1" + "m", "drawable", getPackageName());

        }else if (kbs <=  50000){

            String fileName = converterKbsToIntegerfileNameString(fin);
            i = getResources().getIdentifier("ic_stat_" + fileName + "m", "drawable", getPackageName());

        }else{

            i = R.drawable.ic_stat_50_plus_m;
        }

        builder.setSmallIcon(i);



    }

    private String converterKbsToDecimalfileNameString(Double fin){

        Long j = Math.round(fin/100);
        Double l = j /10.0;
        String k = String.valueOf(l);
        return k.replace(".","_");
    }

    private String converterKbsToIntegerfileNameString(Double fin){

        Long j = Math.round(fin/1000);
        String k = String.valueOf(j);
        return k;
    }
}
