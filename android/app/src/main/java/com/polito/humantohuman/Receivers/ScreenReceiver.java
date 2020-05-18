package com.polito.humantohuman.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.polito.humantohuman.Constants.SCREEN_STATUS;

/**
 * Class that will check if the device has the screen on or off
 */
public class ScreenReceiver extends BroadcastReceiver {
    /**
     * Static value, that will return the current state of the screen
     */
    private static int screenStatus = SCREEN_STATUS.OFF;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_SCREEN_OFF)) { screenStatus = SCREEN_STATUS.OFF;}
        if (action.equals(Intent.ACTION_SCREEN_ON)) { screenStatus = SCREEN_STATUS.ON; }

    }

    /**
     * Synchronized, because it could bea acceded from different threads of the application
     * @return a constant value that could mean ON or OFF
     */
    public static synchronized boolean isScreenOn() { return screenStatus == SCREEN_STATUS.ON; }
    public static synchronized void setScreenStatus(Context context) {
        PowerManager powermanager;
        powermanager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        screenStatus = powermanager.isScreenOn() ? SCREEN_STATUS.ON : SCREEN_STATUS.OFF;
    }
}
