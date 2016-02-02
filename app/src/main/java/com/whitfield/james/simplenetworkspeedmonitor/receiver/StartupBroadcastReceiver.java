package com.whitfield.james.simplenetworkspeedmonitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.home.HomeActivity;
import com.whitfield.james.simplenetworkspeedmonitor.services.NetworkIntentService;

/**
 * Created by jwhit on 19/01/2016.
 */
public class StartupBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("Receiver", "Boot captured");
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preferences_key), Context.MODE_PRIVATE);

        Boolean restart = sharedPreferences.getBoolean(context.getString(R.string.restart_key),false);
        if(restart) {
            Intent intent1 = new Intent(context, NetworkIntentService.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean(HomeActivity.INTENT_TAG_UP, sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_UP, true));
            bundle.putBoolean(HomeActivity.INTENT_TAG_DOWN, sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_DOWN, true));
            bundle.putBoolean(HomeActivity.INTENT_TAG_LOCK_SCREEN, sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_LOCK_SCREEN,true));
            bundle.putBoolean(HomeActivity.INTENT_TAG_RESTART, sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_RESTART,true));
            bundle.putBoolean(HomeActivity.INTENT_TAG_TRAY,sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_TRAY,true));
            bundle.putBoolean(HomeActivity.INTENT_TAG_TRAY_DOWN,sharedPreferences.getBoolean(HomeActivity.INTENT_TAG_TRAY_DOWN,true));
            intent1.putExtras(bundle);

            context.startService(intent1);
            Log.i("Receiver", "Service started");
        }else{

            Log.i("Receiver","Service ignored");
        }
    }
}
