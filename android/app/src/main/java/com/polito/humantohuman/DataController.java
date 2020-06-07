package com.polito.humantohuman;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.polito.humantohuman.Listeners.ReceiverScanFinishedListener;
import com.polito.humantohuman.Listeners.StateChangeListener;
import com.polito.humantohuman.Receivers.BtReceiver;
import com.polito.humantohuman.Services.BGScanService;

import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_NOT_SCANNING;
import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_SCANNING;

/** Class that will manage all the scan types, and all the information received by them
 * It will also send the information to the server and add and delete the specific receiver for
 * the different scans
 *
 */
public class DataController implements ReceiverScanFinishedListener {
    /**
     * Instance of the Current btReceiver that is making the Bt scans
     */
    public final BtReceiver btReceiver;
    private static DataController dataController;
    /**
     * Listener that will received an event if the  current scans have finished
     */
    private StateChangeListener stateChangeListener;

    /** Singleton of DataController, only one possible instance across all the app
     *
     * @return DataController instance
     */
    public static DataController getInstance() {
        if(dataController == null){
            dataController = new DataController();
        }
        return dataController;
    }

    public DataController(){
        this.btReceiver = new BtReceiver();
        btReceiver.setOnFinishedScanListener(this);
    }

    /**We need to add receivers every time the service start running. THe context could change,
     * so that is why we need to add new receivers
     *
     * @param context
     */
    public void addReceivers(Context context) {
    }

    /** Method that will initialize the different scans of the application
     *
     * @param context
     */
    public void startScan(Context context){

        //Toast.makeText(context, context.getString(R.string.start_scanning), Toast.LENGTH_SHORT).show();

        //First we should check that the device is not performing an scan
        if(isScanning()) { return; }
        btReceiver.startScan(context);
        try {
            stateChangeListener.onStateChanged();
        } catch (Exception e) {
            Log.d("Error", "The app has not been started, maybe the device has been rebooted or the app has been updated");
        }

        Log.d("Status ", context.getString(R.string.start_scanning));

    }

    /** Function that return a boolean that check if the device is performing an scan
     *
     * @return a boolean to check if the device is performing any kind of scan or not
     */
    public boolean isScanning(){ return  getStatus() == STATUS_SCANNING; }

    /**Function to get the status of the scan
     *
     * @return A constant value, that will represent the status of the device
     * it could be either Scanning or Not scanning
     */
    public int getStatus() {
        if(btReceiver.getStatus() == STATUS_SCANNING){
            return btReceiver.getStatus();
        }
        return STATUS_NOT_SCANNING;
    }


    /**This will remove the receivers when the scan has finished
     *
     * @param context
     */
    public void removeReceivers(Context context){
    }

    @Override
    public void onScanFinished(BtReceiver.ScanType type, Context context) {
        switch (type) {
            case WIFI: {
                //What to do when the WifiReceiver has finished scanning
                Log.d("Status ", context.getString(R.string.wifi_scann_finished));
                break;
            }
            case BLUETOOTH: {
                //What to do when the BtReceiver has finished scanning

                Log.d("Status ", context.getString(R.string.bt_scann_finished));
                break;
            }
        }

        if(!isScanning()){
            Log.d("Status", "Scan has finished");
            removeReceivers(context);
            btReceiver.clearData();
            BGScanService.setLastScanTime(System.currentTimeMillis());
            try {
                stateChangeListener.onStateChanged();
            } catch (Exception e) {
                Log.d("Error", "The app has not been started, maybe the device has been rebooted or the app has been updated");
            }
        }

    }


    /** It will tell the different receivers to stop the scan
     *
     * @param context
     */
    public void stopScan(Context context){
        btReceiver.stopScan(context);
        removeReceivers(context);
        try {
            stateChangeListener.onStateChanged();
        } catch (Exception e) {
            Log.d("Error", "The app has not been started, maybe the device has been rebooted or the app has been updated");
        }
    }

    public void addScanFinishedListener(StateChangeListener stateChangeListener){
        this.stateChangeListener = stateChangeListener;
    }





}
