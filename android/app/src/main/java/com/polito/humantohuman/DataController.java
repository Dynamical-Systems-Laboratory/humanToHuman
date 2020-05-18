package com.polito.humantohuman;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.polito.humantohuman.ConnsObjects.ConnObject;
import com.polito.humantohuman.ConnsObjects.LocationConn;
import com.polito.humantohuman.Database.ConnDatabase;
import com.polito.humantohuman.HTTPClient.HTTPClient;
import com.polito.humantohuman.HTTPClient.HTTPClientBuilder;
import com.polito.humantohuman.Listeners.ReceiverScanFinishedListener;

import com.polito.humantohuman.Listeners.StateChangeListener;
import com.polito.humantohuman.Receivers.BtReceiver;
import com.polito.humantohuman.Receivers.ScanReceiver;
import com.polito.humantohuman.Receivers.ScreenReceiver;
import com.polito.humantohuman.Receivers.WifiReceiver;
import com.polito.humantohuman.ResponseHandler.UploadDataHandler;
import com.polito.humantohuman.Services.BGScanService;

import java.util.HashMap;
import java.util.Map;


import static com.polito.humantohuman.Constants.TIME.*;

import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_NOT_SCANNING;
import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_SCANNING;
import static com.polito.humantohuman.Constants.SERVER_ENDPOINT.*;

/** Class that will manage all the scan types, and all the information received by them
 * It will also send the information to the server and add and delete the specific receiver for
 * the different scans
 *
 */
public class DataController implements ReceiverScanFinishedListener {
    /**
     * Instance of the ConnObject that must be upload
     */
    public ConnObject connObject;
    /**
     * Instance of the Database
     */
    private ConnDatabase connDatabase;
    /**
     * Instance of the Current btReceiver that is making the Bt scans
     */
    public final BtReceiver btReceiver;
    /**
     * Instance of the Current wifiReceiver that is making the WIFI scans
     */
    public final WifiReceiver wifiReceiver;
    /**
     * Instance of the screenReceiver that is checking for the current display state
     */
    public final ScreenReceiver screenReceiver;
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

    /** We will have only one instance of the connObject field. If we want to have in other class,
     * we should do a copy of it
     *
     * @param connObject
     */
    private DataController(ConnObject connObject){
        this.connObject = connObject;
        this.btReceiver = new BtReceiver();
        this.wifiReceiver = new WifiReceiver();
        this.screenReceiver = new ScreenReceiver();
        //Setting up the listener
        btReceiver.setOnFinishedScanListener(this);
        wifiReceiver.setOnFinishedScanListener(this);
    }
    private DataController() { this(new ConnObject()); }

    /**We need to add receivers every time the service start running. THe context could change,
     * so that is why we need to add new receivers
     *
     * @param context
     */
    public void addReceivers(Context context) {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            context.registerReceiver(screenReceiver,intentFilter);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Method that will initialize the different scans of the application
     *
     * @param context
     */
    public void startScan(Context context){

        //Toast.makeText(context, context.getString(R.string.start_scanning), Toast.LENGTH_SHORT).show();

        //First we should check that the device is not performing an scan
        if(isScanning()) { return; }

        //We get the instance of the database
        this.connDatabase = ConnDatabase.getInstance(context);

        //The device id is set up in ConnObject
        connObject.setIdDevice(Utilities.getSecureId(context));

        //We add the necessary receivers
        addReceivers(context);

        //We tell the receivers that they should do an scan
        wifiReceiver.startScan(context);
        btReceiver.startScan(context);

        //Setting up a initial state of the screen.
        screenReceiver.setScreenStatus(context);

        try {
            stateChangeListener.onStateChanged();
        } catch (Exception e) {
            Log.d("Error", "The app has not been started, maybe the device has been rebooted or the app has been updated");
        }


        //Comment to disable the location of the device
        LocationConn locationConn = LocationConn.getLocation(context);
        connObject.setLocationConn(locationConn);

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
        if(wifiReceiver.getStatus() == STATUS_SCANNING){
            return wifiReceiver.getStatus();
        }
        return STATUS_NOT_SCANNING;
    }


    /**This will remove the receivers when the scan has finished
     *
     * @param context
     */
    public void removeReceivers(Context context){
        try { context.unregisterReceiver(screenReceiver); } catch (Exception e) { }
    }

    @Override
    public void onScanFinished(ScanReceiver.ScanType type, Context context) {
        switch (type) {
            case WIFI: {
                //What to do when the WifiReceiver has finished scanning
                Log.d("Status ", context.getString(R.string.wifi_scann_finished));
                connObject.getWifiConns().addAll(wifiReceiver.wifiConns);
                break;
            }
            case BLUETOOTH: {
                //What to do when the BtReceiver has finished scanning

                Log.d("Status ", context.getString(R.string.bt_scann_finished));

                connObject.getBtConns().addAll(btReceiver.btConns);
                break;
            }
        }

        if(!isScanning()){
            Log.d("Status", "Scan has finished");
            sendData(context);
            removeReceivers(context);
            wifiReceiver.clearData();
            btReceiver.clearData();
            BGScanService.setLastScanTime(System.currentTimeMillis());
            try {
                stateChangeListener.onStateChanged();
            } catch (Exception e) {
                Log.d("Error", "The app has not been started, maybe the device has been rebooted or the app has been updated");
            }


            //startScan(context);
        }

    }

    /** How is going to send the data
     *
     * @param context
     */
    public void sendData(Context context){
        if(!connObject.isEmpty()) {
            ConnObject connObject = this.connObject.getCopy();
            //Since we have made a copy of the connObject instance, we create a new ConnObject
            this.connObject = new ConnObject();
            connObject.setIdDevice(Utilities.getSecureId(context));
            //Making a copy of the object, since it could be removed before it is saved
            //We insert the object on the database
            connDatabase.insertData(connObject);
            if(!WifiReceiver.isWifiConnected(context)) {
                Log.d("Status", "There is not a wifi connection, so data has been directly saved");
                return;
            }
            try {
                //The data will be uploaded, in case there is an error, it will catch the exception
                //and because the data has been stored we don't need to do anything in there.
                uploadData(connObject,context).post();
            } catch (HTTPClient.InternetError internetError) {
                internetError.printStackTrace();
            }
        }
    }
    /** It will tell the different receivers to stop the scan
     *
     * @param context
     */
    public void stopScan(Context context){
        btReceiver.stopScan(context);
        wifiReceiver.stopScan(context);
        removeReceivers(context);
        try {
            stateChangeListener.onStateChanged();
        } catch (Exception e) {
            Log.d("Error", "The app has not been started, maybe the device has been rebooted or the app has been updated");
        }
    }
    /**How will be the httpclient behaviour
     *
     * @param connObject We pass a copy of a ConnObject
     * @param context
     * @return HTTPClient
     */
    private HTTPClient uploadData(ConnObject connObject, Context context) {
        Map<String,ConnObject> connMap= new HashMap<>();
        connMap.put("conn_info",connObject);
        return new HTTPClientBuilder(context,new UploadDataHandler(connObject,context))
                .setJsonHeader()
                .setJson(Utilities.createJson(connMap))
                .setUrl(CONN_INFO_ENDPOINT)

                .setTimeOut(TIME_OUT)
                .setRetriesAndTimeout(1,TIME_OUT)
                .setResponseTimeOut(RESPONSE_TIME_OUT)
                .addAuth()
                .build();

    }

    public void addScanFinishedListener(StateChangeListener stateChangeListener){
        this.stateChangeListener = stateChangeListener;
    }





}
