package com.whitfield.james.simplenetworkspeedmonitor.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.manager.ApplicationTrafficMonitorActivity;
import com.whitfield.james.simplenetworkspeedmonitor.services.NetworkIntentService;
import com.whitfield.james.simplenetworkspeedmonitor.util.NavBarUtil;

/**
 * Created by jwhit on 18/01/2016.
 */
public class HomeActivity extends AppCompatActivity implements HomeActivityInterface {


    public static final String INTENT_TAG_LOCK_SCREEN = "LOCK_SCREEN";
    public static final String INTENT_TAG_TRAY  = "TRAY";
    public static final String INTENT_TAG_TRAY_DOWN  = "TRAY_DOWN";
    public static final String INTENT_TAG_SPLIT = "TRAY_SPLIT";
    private final String HOME_ACTIVITY_TAG = "HOME_ACTIVITY";
    public static final String INTENT_TAG_UP = "UP";
    public static final String INTENT_TAG_DOWN = "DOWN";
    public static final String INTENT_TAG_RESTART = "RESTART";

    private Fragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_single_fragment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        fab.setVisibility(View.GONE);

        NavBarUtil navBarUtil = new NavBarUtil(this);
        navBarUtil.setup();


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
    public void startService(boolean up, boolean down, boolean restart, boolean lockScreen, boolean tray, boolean trayDown, boolean split) {

        Intent intent = new Intent(getApplicationContext(), NetworkIntentService.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_TAG_UP,up);
        bundle.putBoolean(INTENT_TAG_DOWN, down);
        bundle.putBoolean(INTENT_TAG_LOCK_SCREEN,lockScreen);
        bundle.putBoolean(INTENT_TAG_RESTART, restart);
        bundle.putBoolean(INTENT_TAG_TRAY,tray);
        bundle.putBoolean(INTENT_TAG_TRAY_DOWN,trayDown);
        bundle.putBoolean(INTENT_TAG_SPLIT,split);
        intent.putExtras(bundle);

        startService(intent);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preferences_key),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.restart_key),restart);
        editor.putBoolean(INTENT_TAG_UP,up);
        editor.putBoolean(INTENT_TAG_DOWN,down);
        editor.putBoolean(INTENT_TAG_LOCK_SCREEN,lockScreen);
        editor.putBoolean(INTENT_TAG_TRAY,tray);
        editor.putBoolean(INTENT_TAG_TRAY_DOWN,trayDown);
        editor.putBoolean(INTENT_TAG_SPLIT,split);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.application_traffic_monitor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
