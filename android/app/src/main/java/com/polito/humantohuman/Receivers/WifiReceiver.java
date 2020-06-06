package com.polito.humantohuman.Receivers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.polito.humantohuman.ConnsObjects.WifiConn;

import java.util.ArrayList;
import java.util.List;

import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_NOT_SCANNING;
import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_SCANNING;
import static com.polito.humantohuman.Constants.TIME.MAX_TIME_WIFI_SCAN;


/** This class will handle the wifi scan.
 *
 */
public class WifiReceiver extends ScanReceiver {
    /**
     * With the wifilock, the application will try to keep the Wifi scans working even if the user
     * has not been using the device during a long period of time
     */
    private WifiManager.WifiLock wifiLock;
    /**
     * Manager that will perform the wifiScans
     */
    private  WifiManager wifiManager;
    /**
     * List of the current wifi beacons found by the device
     */
    public final List<WifiConn> wifiConns;



    public WifiReceiver() { super(ScanType.WIFI, 6); this.wifiConns = new ArrayList<>(); }

    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

        if(success && getStatus() == STATUS_SCANNING){ scanFinished(context); }


    }

    @Override
    protected void onStartScan(Context context) {
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        wifiLock =  wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "WifiLock");
        //wifiManager.setWifiEnabled(true);
        if(!wifiManager.isWifiEnabled()) {return;}
        if (wifiManager.startScan()) {
            setStatus(STATUS_SCANNING);
            instant = System.currentTimeMillis();
            scanTimer(context, MAX_TIME_WIFI_SCAN);

        }
    }

    @Override
    public void onScanFinished() {
        List<ScanResult> resultList = wifiManager.getScanResults();
        //Add the items to WifiConn list.
        int a = 1;
        for(int i = 0; i < resultList.size(); i++) {
            ScanResult sc = resultList.get(i);
            wifiConns.add(new WifiConn(sc.BSSID, sc.level));
        }
        setStatus(STATUS_NOT_SCANNING);

    }

    @Override
    public boolean checkPermissions(Context context) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void clearData() { wifiConns.clear(); }

    @Override
    public void stopScan(Context context) {
        scanFinished(context);
        try {
            wifiLock.release();
        } catch (RuntimeException e) {
            Log.d("Error", "WifiLock Was null, nothing was acquire");
        }

    }

    @Override
    protected void addReceivers(Context context) {
        try{
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            intentFilter.addAction(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
            context.registerReceiver(this,intentFilter);
        } catch (Exception e){ e.printStackTrace(); }
    }

    /**
     * Check if the user wants to upload the data using data or only wifi
     * @param context
     * @return
     */
    public static boolean isUploadByWifiEnable(Context context) {
        SharedPreferences sh = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        //First check that the user wants to upload the data only using wifi
        return sh.getBoolean("wifi",false);
    }

    /**
     * Check if the wifi is connected or not
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if(wifiInfo.getNetworkId() == -1) { return false; }
            return true;
        }
        return false;
    }
}
