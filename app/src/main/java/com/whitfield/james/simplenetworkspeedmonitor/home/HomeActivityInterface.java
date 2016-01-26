package com.whitfield.james.simplenetworkspeedmonitor.home;

/**
 * Created by jwhit on 18/01/2016.
 */
public interface HomeActivityInterface {

    public void startService(boolean up, boolean down, boolean restart, boolean lockScreen, boolean tray, boolean trayDown);
    public void stopService();

}
