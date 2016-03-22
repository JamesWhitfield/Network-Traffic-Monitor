package com.whitfield.james.simplenetworkspeedmonitor.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.home.HomeActivity;
import com.whitfield.james.simplenetworkspeedmonitor.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

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

    private static final String TOTAL_RECEIVED = "ReceivedTotal";
    private static final String TOTAL_TRANSMITTED = "TransmittedTotal";
    private static final String MOBILE_RECEIVED = "ReceivedMobile";
    private static final String MOBILE_TRANSMITTED = "TransmittedMobile";
    private static final String TOTAL_RECEIVED_LAST = "ReceivedTotalLast";
    private static final String TOTAL_TRANSMITTED_LAST = "TransmittedTotalLast";
    private static final String MOBILE_RECEIVED_LAST = "ReceivedMobileLast";
    private static final String MOBILE_TRANSMITTED_LAST = "TransmittedMobileLast";

    private static final int NOTIFICATION_ID = 1;
    public static final String NAME = "NetworkService";
    Boolean up, down, lockScreen, tray, trayDown = null;
    Boolean split = false;
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

    private void startService() {

        Bundle bundle = intent.getExtras();

        if (bundle.containsKey(HomeActivity.INTENT_TAG_DOWN)) {
            down = bundle.getBoolean(HomeActivity.INTENT_TAG_DOWN);
        }
        if (bundle.containsKey(HomeActivity.INTENT_TAG_UP)) {
            up = bundle.getBoolean(HomeActivity.INTENT_TAG_UP);
        }
        if (bundle.containsKey(HomeActivity.INTENT_TAG_LOCK_SCREEN)) {
            lockScreen = bundle.getBoolean(HomeActivity.INTENT_TAG_LOCK_SCREEN);
        }
        if (bundle.containsKey(HomeActivity.INTENT_TAG_TRAY)) {
            tray = bundle.getBoolean(HomeActivity.INTENT_TAG_TRAY);
        } else {
            tray = false;
        }
        if (bundle.containsKey(HomeActivity.INTENT_TAG_TRAY_DOWN)) {
            trayDown = bundle.getBoolean(HomeActivity.INTENT_TAG_TRAY_DOWN);
        } else {
            trayDown = false;
        }
        if (bundle.containsKey(HomeActivity.INTENT_TAG_SPLIT)) {
            split = bundle.getBoolean(HomeActivity.INTENT_TAG_SPLIT);
        } else {
            split = false;
        }

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Network speed")
//                    .setContentText("Setup...")
                .setSmallIcon(R.drawable.ic_stat_)

                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SYSTEM);


        if(tray){
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        }else{

            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        }

        if (lockScreen == true) {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        }

        Intent intent1 = new Intent(getBaseContext(), HomeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(intent1);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);


        startForeground(NOTIFICATION_ID, builder.build());


        timer = new Timer();
        timer.scheduleAtFixedRate(new NetworkTrafficChangeTimerTask(builder), 0, 1000);

    }

    class NetworkTrafficChangeTimerTask extends TimerTask {

        private NotificationCompat.Builder builder;
        NotificationManager notificationManager;
        Long downStartTotal ,
                upStartTotal ,
                downStartMobile ,
                upStartMobile ;

        public NetworkTrafficChangeTimerTask(android.support.v7.app.NotificationCompat.Builder builder){

            this.builder = builder;
            this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            this.downStartMobile = TrafficStats.getMobileRxBytes();
            this.upStartMobile = TrafficStats.getMobileTxBytes();
            this.downStartTotal = TrafficStats.getTotalRxBytes();
            this.upStartTotal = TrafficStats.getTotalTxBytes();

        }








        @Override
        public void run() {

            //Get live data

            Long downCurrentMobile = TrafficStats.getMobileRxBytes();
            Long upCurrentMobile = TrafficStats.getMobileTxBytes();
            Long downCurrentTotal = TrafficStats.getTotalRxBytes();
            Long upCurrentTotal = TrafficStats.getTotalTxBytes();


            if(downCurrentTotal < downStartTotal || upCurrentTotal < upStartTotal){
                Log.i("Error","");
            }
            if(downCurrentMobile < downStartMobile || upCurrentMobile < downStartMobile){
                Log.i("Error","");
            }

            JSONObject liveData = new JSONObject();
            try {

                liveData.put(TOTAL_RECEIVED, downCurrentTotal);
                liveData.put(TOTAL_TRANSMITTED, upCurrentTotal);
                liveData.put(MOBILE_RECEIVED, downCurrentMobile);
                liveData.put(MOBILE_TRANSMITTED, upCurrentMobile);
                liveData.put(TOTAL_RECEIVED_LAST, downStartTotal);
                liveData.put(TOTAL_TRANSMITTED_LAST, upStartTotal);
                liveData.put(MOBILE_RECEIVED_LAST, downStartMobile);
                liveData.put(MOBILE_TRANSMITTED_LAST, upStartMobile);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Metrics and notification update
            update(liveData, builder, notificationManager);

            //Prepare for next iteration
            downStartTotal = downCurrentTotal;
            upStartTotal = upCurrentTotal;
            downStartMobile = downCurrentMobile;
            upStartMobile = upCurrentMobile;
        }
    }



    private void update(JSONObject liveData, NotificationCompat.Builder builder, NotificationManager notificationManager){

        //This function calculated all of the changes since the last call.
        try {
            //Calculate totals for step
            Long totalReceived ;
            Long totalTransmitted = 0l;
            Long mobileReceived = 0l;
            Long mobileTransmitted = 0l;
            Long wifiReceived = 0l;
            Long wifiTransmitted = 0l;


            totalReceived = (liveData.getLong(TOTAL_RECEIVED) - liveData.getLong(TOTAL_RECEIVED_LAST));
            totalTransmitted = (liveData.getLong(TOTAL_TRANSMITTED) - liveData.getLong(TOTAL_TRANSMITTED_LAST));
            mobileReceived = (liveData.getLong(MOBILE_RECEIVED) - liveData.getLong(MOBILE_RECEIVED_LAST));
            mobileTransmitted = (liveData.getLong(MOBILE_TRANSMITTED) - liveData.getLong(MOBILE_TRANSMITTED_LAST));

            wifiReceived = totalReceived - mobileReceived;
            wifiTransmitted = totalTransmitted - mobileTransmitted;

            if(mobileReceived < 0 || wifiReceived < 0){
                Log.i("Error","Negative value");
            }


//            logNetworkChange(totalReceived, totalTransmitted, mobileReceived, mobileTransmitted, wifiReceived, wifiTransmitted);



            //Update notification tray icon
            if(tray){

                if(trayDown){
                    setSmallIcon(builder, totalReceived/1024);
                }else{
                    setSmallIcon(builder, totalTransmitted/1024);
                }
            }

            //Simple output
            if(!split) {
                //Simple notification view
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // only for gingerbread and newer versions
                    String output = returnSimpleStringOutput(totalReceived / 1024, totalTransmitted / 1024);
                    RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_simple);
                    remoteViews.setTextViewText(R.id.tvContent, output);
                    builder.setContent(remoteViews);
                }else{
                    //TODO OLD
                    String output = returnSimpleStringOutput(totalReceived / 1024, totalTransmitted / 1024);
                    RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_simple_old);
                    remoteViews.setTextViewText(R.id.tvContent, output);

                    builder.setContent(remoteViews);
                }
            }else{
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // only for gingerbread and newer versions
                    //detailed view
                    RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_detailed);

                    String mobileOutput = "";
                    String wifiOutput = "";
                    if(down & up){

                        wifiOutput = Common.stringDownNotificationOutput(wifiReceived) + "    " + Common.stringUpNotificationOutput(wifiTransmitted);
                        mobileOutput = Common.stringDownNotificationOutput(mobileReceived) + "    " + Common.stringUpNotificationOutput(mobileTransmitted);
                    }else if(up){
                        wifiOutput = Common.stringUpNotificationOutput(wifiTransmitted);
                        mobileOutput =  Common.stringUpNotificationOutput(mobileTransmitted);
                    }else if(down){
                        wifiOutput = Common.stringDownNotificationOutput(wifiReceived);
                        mobileOutput = Common.stringDownNotificationOutput(mobileReceived);
                    }else{
                        wifiOutput = "Setting required";
                        mobileOutput =  "Setting required";
                    }
                    remoteViews.setTextViewText(R.id.tvMobile,  mobileOutput);
                    remoteViews.setTextViewText(R.id.tvWifi, wifiOutput);
                    builder.setContent(remoteViews);
                }else {
                    //TODO OLD
                    // only for gingerbread and newer versions
                    //detailed view
                    RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_detailed_old);

                    String mobileOutput = "";
                    String wifiOutput = "";
                    if(down & up){

                        wifiOutput = Common.stringDownNotificationOutput(wifiReceived) + "    " + Common.stringUpNotificationOutput(wifiTransmitted);
                        mobileOutput = Common.stringDownNotificationOutput(mobileReceived) + "    " + Common.stringUpNotificationOutput(mobileTransmitted);
                    }else if(up){
                        wifiOutput = Common.stringUpNotificationOutput(wifiTransmitted);
                        mobileOutput =  Common.stringUpNotificationOutput(mobileTransmitted);
                    }else if(down){
                        wifiOutput = Common.stringDownNotificationOutput(wifiReceived);
                        mobileOutput = Common.stringDownNotificationOutput(mobileReceived);
                    }else{
                        wifiOutput = "Setting required";
                        mobileOutput =  "Setting required";
                    }
                    remoteViews.setTextViewText(R.id.tvMobile,  mobileOutput);
                    remoteViews.setTextViewText(R.id.tvWifi, wifiOutput);
                    builder.setContent(remoteViews);
                }
            }


            builder.setShowWhen(false);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean logNetworkChange(Long totalReceived, Long totalTransmitted, Long mobileReceived, Long mobileTransmitted, Long wifiReceived, Long wifiTransmitted){

        if(totalReceived > 0 ||
                totalTransmitted > 0 ||
                mobileReceived > 0 ||
                mobileTransmitted > 0 ||
                wifiReceived > 0 ||
                wifiTransmitted > 0){
            Log.i("Network", "Action Detected");
            Log.i("Network", "Total: " + Common.stringDownNotificationOutput(totalReceived) + "  " + Common.stringUpNotificationOutput(totalTransmitted) );
            Log.i("Network", "Mobile: "  +Common.stringDownNotificationOutput(mobileReceived)+  "  " + Common.stringUpNotificationOutput(mobileTransmitted));
            Log.i("Network", "WIFI: "  +Common.stringDownNotificationOutput(wifiReceived)+  "  " + Common.stringUpNotificationOutput(wifiTransmitted));
            Log.i("Network","------------------------------------------");
            return true;
        }else{
            return false;
        }


    }





    private String returnSimpleStringOutput(Long totalReceivedKbs, Long totalTransmittedKbs){

        String transmitString = "";
        String receivedString = "";


        if(down){
            if(totalReceivedKbs > 1024){
                long mbs = totalReceivedKbs/1024;
                receivedString =  "\u25bc " + mbs + "/Mbs";
            }else{

                receivedString = "\u25bc "+ totalReceivedKbs + "/Kbs";
            }
        }
        //Upload Output
        if(up){
            if(totalTransmittedKbs > 1024){
                long mbs = totalTransmittedKbs/1024;
                transmitString = "\u25b2 " + mbs + "/Mbs";
            }else{
                transmitString = "\u25b2 " + totalTransmittedKbs + "/kbs";
            }
        }

        String output;
        if(transmitString.length() > 0 && receivedString.length() >0){
            output = receivedString + "    " + transmitString;
        }else{
            output = receivedString + transmitString;
        }
        /*if(totalReceivedKbs > 0 || totalTransmittedKbs > 0) {
            Log.i("OUTPUT", output);
        }*/
        return output;
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
