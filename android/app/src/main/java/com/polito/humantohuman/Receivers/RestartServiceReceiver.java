package com.polito.humantohuman.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.polito.humantohuman.Constants;
import com.polito.humantohuman.Services.BGScanService;

/**
 * Service that will be called, if the scan has been stuck in some part.
 * It is useful, in case some alarms, handlers don't work, because of the current state of the device
 */
public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long lastScan = intent.getLongExtra("last_scan",0);
        //We stop the service, then we rerun the service
        /**
         * First we check that the timestamp has changed
         */
        if(BGScanService.getLastScanTime() != lastScan || lastScan == 0) {
           Log.d("Status", "Scan started at " + lastScan + " has finished correctly");
           return;
        }
        Log.d("Alarm:", "Device in doze, running the scan from the alarm system");
        /**
         *  In the case where the service has been stuck, it is restarted
         */
        Intent intent1 = new Intent(context,BGScanService.class);
        intent1.setAction(Constants.SERVICE_ACTION.RESTART_FOREGROUND_INTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent1);
        } else {
            context.startService(intent1);
        }
    }
}
