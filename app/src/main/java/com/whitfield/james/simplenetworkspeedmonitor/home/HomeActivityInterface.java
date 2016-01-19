package com.whitfield.james.simplenetworkspeedmonitor.home;

/**
 * Created by jwhit on 18/01/2016.
 */
public interface HomeActivityInterface {

    public void startService(Boolean up,Boolean down,Boolean restart,Boolean lockScreen);
    public void stopService();

}
