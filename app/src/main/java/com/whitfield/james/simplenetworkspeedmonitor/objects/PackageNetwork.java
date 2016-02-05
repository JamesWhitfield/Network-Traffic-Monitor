package com.whitfield.james.simplenetworkspeedmonitor.objects;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;

/**
 * Created by jwhit on 05/02/2016.
 */
public class PackageNetwork {

    private ApplicationInfo applicationInfo;
    private Long bytesReceievd;
    private Long bytesTransmitted;
    private int uid;
    private Drawable icon;
    private String nameOrPackage;

    public PackageNetwork(ApplicationInfo applicationInfo, PackageManager packageManager){

        this.applicationInfo = applicationInfo;

        this.bytesReceievd = TrafficStats.getUidRxBytes(applicationInfo.uid);
        this.bytesTransmitted = TrafficStats.getUidTxBytes(applicationInfo.uid);

        try {
            icon = packageManager.getApplicationIcon(applicationInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        nameOrPackage = (String) applicationInfo.loadDescription(packageManager);
        if(nameOrPackage == null) {
            nameOrPackage = applicationInfo.packageName;
        }

    }

    public void  updateNetworkDetails(){

        bytesReceievd = TrafficStats.getUidRxBytes(applicationInfo.uid);
        bytesTransmitted = TrafficStats.getUidTxBytes(applicationInfo.uid);

    }

    public Long getBytesReceievd(){
        return bytesReceievd;
    }

    public Long getBytesTransmitted(){
        return bytesTransmitted;
    }

    public String getNameOrPackage(){
        return nameOrPackage;
    }

    public Drawable getIcon(){
        return icon;
    }
}
