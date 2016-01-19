package com.whitfield.james.simplenetworkspeedmonitor.home;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.services.NetworkIntentService;

/**
 * Created by jwhit on 18/01/2016.
 */
public class HomeActivity extends AppCompatActivity implements HomeActivityInterface {


    public static final String INTENT_TAG_LOCK_SCREEN = "LOCK_SCREEN";
    private final String HOME_ACTIVITY_TAG = "HOME_ACTIVITY";
    public static final String INTENT_TAG_UP = "UP";
    public static final String INTENT_TAG_DOWN = "DOWN";
    public static final String INTENT_TAG_RESTART = "RESTART";
    public static final String INTENT_TAG_STOP = "STOP";

    private Fragment homeFragment;
    ComponentName name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.fragment_single_pane_layout);


        if (savedInstanceState != null) {
            homeFragment = getSupportFragmentManager().findFragmentByTag(HOME_ACTIVITY_TAG);
        } else {

            homeFragment = new HomeFragment();

            //loginFragment.setArguments(getIntent().getExtras());

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.add(R.id.fragmentContainer, homeFragment, HOME_ACTIVITY_TAG);
            fragmentTransaction.commit();
        }


    }


    @Override
    public void startService(Boolean up,Boolean down,Boolean restart,Boolean lockScreen) {

        Intent intent = new Intent(getApplicationContext(), NetworkIntentService.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_TAG_UP,up);
        bundle.putBoolean(INTENT_TAG_DOWN, down);
        bundle.putBoolean(INTENT_TAG_RESTART, restart);
        bundle.putBoolean(INTENT_TAG_LOCK_SCREEN,lockScreen);
        intent.putExtras(bundle);
        name = startService(intent);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences_key),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.restart_key),restart);
        editor.putBoolean(INTENT_TAG_UP,up);
        editor.putBoolean(INTENT_TAG_DOWN,down);
        editor.putBoolean(INTENT_TAG_LOCK_SCREEN,lockScreen);
        editor.commit();

        Log.i("Preferences",sharedPreferences.getAll().toString());

        Log.i(HOME_ACTIVITY_TAG,"Start service");
    }

    @Override
    public void stopService() {

        Intent intent = new Intent(getApplicationContext(), NetworkIntentService.class);
        Boolean b = stopService(intent);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences_key),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.restart_key),false);
        editor.commit();

        Log.i(HOME_ACTIVITY_TAG,"Stop service:" +b);
    }
}
