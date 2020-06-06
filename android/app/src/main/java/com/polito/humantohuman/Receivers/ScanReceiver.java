package com.polito.humantohuman.Receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.polito.humantohuman.Listeners.ReceiverScanFinishedListener;
import com.polito.humantohuman.Utilities;

import java.util.HashSet;

import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_NOT_SCANNING;
import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_SCANNING;


/**
 * This is an abstract class that defines the methods and fields for every kind of scan
 */
public abstract class ScanReceiver extends BroadcastReceiver {
    /**
     * Enumerator that defines the type of scan used
     */
    protected final ScanType scanType;
    /**
     * Value that defines the status of the scan
     */
    private int status = STATUS_NOT_SCANNING;
    /**
     * List of the current listeners that are waiting to the scan to finish
     */
    protected HashSet<ReceiverScanFinishedListener> setListener = new HashSet<>();
    /**
     * Instant where last scan has been started
     */
    public long instant;
    /**
     * Constant value, that must be different for every type os ScanReceiver, in order to don't overlap
     * different alarms.
     */
    public final int requestCode;

    protected ScanReceiver(ScanType scanType, int requestCode) {
        this.scanType = scanType;
        this.requestCode = requestCode;
    }



    /**
     * In which condition is the adapter at the moment Use class constants to determine the different states
     * @return a constant value that could mean either Scanning or Not scanning
     */
    public int getStatus() { return status;}

    /**
     *
     * @param status Constant that could mean either Scanning or Not scanning
     */
    protected void setStatus(int status){this.status = status;}

    protected abstract void onStartScan(Context context);

    /**
     * What the device should for start scanning
     * @param context
     */
    public void startScan(Context context) {
        addReceivers(context);
        onStartScan(context);
    }


    /**
     * Call the method to handle first the information and call the listener once the scannn has finished
     * @param context
     */
    protected void scanFinished(Context context) {
        if(status == STATUS_SCANNING) {
            onScanFinished();
            notifyListener(context);
            instant = System.currentTimeMillis();
            remoReceivers(context);
        }
    }

    /**
     * What to do when the scan has finished
     */
    protected abstract void onScanFinished();

    /**
     * Notify the listener that the scan has finished
     * @param context
     */
    protected void notifyListener(Context context){
        this.status = STATUS_NOT_SCANNING;
        for (ReceiverScanFinishedListener listener : setListener)  {
            listener.onScanFinished(scanType, context);
        }
    }

    /**
     * What to do when the scann is finished
     * @param context
     * @return boolean, in case that the permission has been accepted or not
     */
    public abstract boolean checkPermissions(Context context);

    /**
     * To set up the listener
     * @param listener
     */
    public  void setOnFinishedScanListener(ReceiverScanFinishedListener listener) {this.setListener.add(listener); }

    /**
     * To clear the list data, once it has been saved in ConnObject of DataController
     */
    public abstract void clearData();

    /**
     *  What to do when we want to stop the scan, or when the scan has been finished
     * @param
     */

    public abstract void stopScan(Context context);

    /**
     * This method will prevent to be stuck during an scan. In case that the instant doesn't change
     * it will force stop, so it could be run other time the scan service.
     * @param context
     * @param maxTime this value, should depend on the device and also should be lower than the time
     *                between the start of an scan and the start of the other scan
     */
    protected void scanTimer(Context context,long maxTime){
        Intent intent = new Intent(context, StopScanReceiver.class);
        intent.setAction(scanType.name());
        intent.putExtra("last_scan",instant);
        intent.putExtra("type",scanType.name());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,requestCode,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Utilities.setAlarm(context,pendingIntent,maxTime);
    }

    protected abstract void addReceivers(Context context);
    protected void remoReceivers(Context context) {
        try{ context.unregisterReceiver(this); } catch (Exception e)  {
            Log.d("Error", "Receiver from " + scanType.toString() + " not removed");
        }
    }

    public synchronized long getInstant() {return instant;}


    public enum ScanType {BLUETOOTH, WIFI}


}
