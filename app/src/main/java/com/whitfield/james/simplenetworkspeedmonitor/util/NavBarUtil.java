package com.whitfield.james.simplenetworkspeedmonitor.util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.whitfield.james.simplenetworkspeedmonitor.R;
import com.whitfield.james.simplenetworkspeedmonitor.home.HomeActivity;
import com.whitfield.james.simplenetworkspeedmonitor.manager.ApplicationTrafficMonitorActivity;
import com.whitfield.james.simplenetworkspeedmonitor.tools.ToolsActivity;

/**
 * Created by jwhit on 12/02/2016.
 */
public class NavBarUtil implements NavigationView.OnNavigationItemSelectedListener{

    Activity activity;
    DrawerLayout drawer;
    Toolbar toolbar;
    NavigationView navigationView;

    public NavBarUtil(Activity activity){

        this.activity = activity;

        drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        navigationView = (NavigationView) activity.findViewById(R.id.nav_view);

    }

    public void setup(){

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tvVersion);
        PackageInfo pInfo = null;
        try {
            pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        if(version != null){

            textView.setText("Version: " + version);
        }

        navigationView.setNavigationItemSelectedListener(this);


    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_live) {
            // Move to live
            Intent intent = new Intent(activity,HomeActivity.class);
            activity.startActivity(intent);
        } else if (id == R.id.nav_applications) {
            // Move to application
            Intent intent = new Intent(activity,ApplicationTrafficMonitorActivity.class);
            activity.startActivity(intent);
        }else if (id == R.id.nav_tools) {
            // Move to application
            Intent intent = new Intent(activity,ToolsActivity.class);
            activity.startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
