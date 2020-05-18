package com.polito.humantohuman.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.polito.humantohuman.DataController;
import com.polito.humantohuman.Utilities;

import static com.polito.humantohuman.Receivers.ScanReceiver.ScanType.BLUETOOTH;
import static com.polito.humantohuman.Receivers.ScanReceiver.ScanType.WIFI;

/**
 * This Receiver will stop the scan of a receiver in the case that it didn't manage to make the full scan
 */
public class StopScanReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Utilities.initializeApp(context);
        String type = intent.getStringExtra( "type");
        long lastScan = intent.getLongExtra("last_scan",0);
        Log.d("Alarm","Check if scan is completed " + type);
        DataController dataController = DataController.getInstance();

        if(type.equals(BLUETOOTH.name())) {
            if(dataController.btReceiver.getInstant() == lastScan){
                Log.d("Status", "Bluetooth Cancelled");
                dataController.btReceiver.stopScan(context);
            }
        }
        if(type.equals(WIFI.name())) {
            if(dataController.wifiReceiver.getInstant() == lastScan){
                dataController.wifiReceiver.stopScan(context);
                Log.d("Status", "Wifi Cancelled");
            }
        }

    }
}
