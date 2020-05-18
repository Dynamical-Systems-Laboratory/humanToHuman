package com.polito.humantohuman.Services;

import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.polito.humantohuman.ConnsObjects.ConnObject;
import com.polito.humantohuman.Constants;
import com.polito.humantohuman.Database.ConnDatabase;
import com.polito.humantohuman.HTTPClient.HTTPClient;
import com.polito.humantohuman.HTTPClient.HTTPClientBuilder;
import com.polito.humantohuman.R;
import com.polito.humantohuman.Receivers.WifiReceiver;
import com.polito.humantohuman.ResponseHandler.UploadSaveDataHandler;
import com.polito.humantohuman.Utilities;

import java.util.HashMap;
import java.util.Map;

import static com.polito.humantohuman.Constants.SERVER_ENDPOINT.CONN_INFO_ENDPOINT;
import static com.polito.humantohuman.Constants.TIME.RESPONSE_TIME_OUT;
import static com.polito.humantohuman.Constants.TIME.SEND_SAVE_DATA_THREAD_TIME;
import static com.polito.humantohuman.Constants.TIME.TIME_OUT;


/**
 * Runnable that will be executed to upload the data
 */
public class UploadRunnable implements Runnable {
    //Variables that will be flags the state of the service
    //To change the value of this variables we use synchronized function to be threadsafe
    /**
     * Current state of the HTTP client
     */
    public static boolean IS_SENDING_DATA = false;
    /**
     * Current state of the service
     */
    public static boolean SERVICE_IS_RUNNING = false;
    /**
     * Timer that checks, if it has pass X time to upload the data
     */
    private static long timer = 0;

    public static synchronized void setIsSendingData(boolean t) { IS_SENDING_DATA = t; }

    public static synchronized void setServiceIsRunning(boolean t) { SERVICE_IS_RUNNING = t; }

    public synchronized static long getTimer() { return timer; }

    public synchronized static void setTimer(long timer) { UploadRunnable.timer = timer; }

    /**
     * The current context, from where it has been executed
     */
    private final Service service;
    /**
     * Instance of the Database
     */
    private ConnDatabase connDatabase;
    /**
     * Number of correct uploads that has been performed
     */
    private int uploads = 0;

    public UploadRunnable(int uploads, Service service) {
        this.uploads = uploads;
        this.service = service;
    }

    /**
     * HTTP client, that will upload the data
     *
     * @param connObject
     * @param context
     * @return
     */
    private HTTPClient uploadSavedData(ConnObject connObject, Context context) {
        Map<String, ConnObject> connMap = new HashMap<>();
        connMap.put("conn_info", connObject);
        return new HTTPClientBuilder(context, new UploadSaveDataHandler(connObject, context))
                .setJsonHeader()
                .setJson(Utilities.createJson(connMap))
                .setUrl(CONN_INFO_ENDPOINT)
                .setTimeOut(TIME_OUT)
                .setRetriesAndTimeout(1, TIME_OUT)
                .setResponseTimeOut(RESPONSE_TIME_OUT)
                .addAuth()
                .build();
    }

    //TODO look if change 15, next 30, next 60

    @Override
    public void run() {

        if (!SERVICE_IS_RUNNING) {
            service.stopSelf();
            return;
        }
        connDatabase = ConnDatabase.getInstance(service);
        try {
            if (!connDatabase.isEmpty()) {
                /** First the application makes sure that the device is under a wifi network **/
                if (!WifiReceiver.isWifiConnected(service)) {
                    // When the device is using data connection, and the option is allowed
                    //If we have upload x objects, we exit
                    Log.d("Status", "Trying to upload the data using phone data");

                    //The application checks if it has been achieved the maximum items to upload
                    //and if there the number of items stored overpass the limit set in Constants.MAX_ITEM_DATABASE
                    if (uploads == Constants.MAX_UPLOADS || ConnDatabase.getInstance(service).rows() <= Constants.MAX_ITEMS_DATABASE) {
                        throw new Exception("Max uploads reached or not enough data to send");
                    }
                    //If we are under a data connection, the number of uploads is incremented
                    uploads++;
                }

                //This checks if the device is still waiting for a response from the server from the last petition
                //It also checks if the service is still running
                if (!IS_SENDING_DATA && SERVICE_IS_RUNNING) {
                    //If the conditions are satisfied, the a new petition is created.
                    ConnObject connObject = connDatabase.getFirst();
                    uploadSavedData(connObject, service).post();
                    setIsSendingData(true);
                }
                //It could be the case where the server didn't give a response yet, so we try to
                //make another call to the handler
                Handler handler = new Handler();
                handler.postDelayed(new UploadRunnable(uploads, service), SEND_SAVE_DATA_THREAD_TIME);
            } else {
                //If the database is empty
                setServiceIsRunning(false);
                service.stopSelf();
                setTimer(0);
            }
        } catch (Exception e) {
            //If an exception is thrown
            Log.d("Error", e.getMessage());
            setServiceIsRunning(false);
            setIsSendingData(false);
            service.stopSelf();
            setTimer(0);
        }
    }
}