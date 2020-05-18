package com.polito.humantohuman.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.polito.humantohuman.Constants;
import com.polito.humantohuman.Services.BGScanService;
import com.polito.humantohuman.Utilities;

import static com.polito.humantohuman.Constants.TIME.NOTIFY_INTERNET_INTERVAL;

/**
 * Receiver that will handle the service, when the device has been power on
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = "";
        try {
            /**
             * We check fom where we received the intent, by checking the action
             */
            action = intent.getAction();
        } catch (NullPointerException exception) {
            action = "";
        }
        /**
         * In the case where the application is updated, because we dont know if the service was running
         * or not, we must try to stop the background service
         */
        if(action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)){ stopService(context); }
        /**
         * In the both cases (BOOT_COMPLETED or MY_PACKAGE_REPLACED) the service must be reruned
         */
        startService(context);

    }


    /**
     * Used for stop the background service (if it was running)
     * @param context
     */
    private void stopService(Context context){
        Intent serviceIntent = new Intent(context, BGScanService.class);
        serviceIntent.setAction(Constants.SERVICE_ACTION.STOP_FOREGROUND_INTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UploadReceiver.cancelJob(context);
        }
        //context.startService(serviceIntent);
        context.stopService(serviceIntent);
    }

    /**
     * Mehtod that start the service
     * @param context
     */
    private void startService(Context context) {
        Intent serviceIntent = new Intent(context, BGScanService.class);
        serviceIntent.setAction(Constants.SERVICE_ACTION.START_FOREGROUND_INTENT);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
                Log.d("Status", "Starting service from receiver");
            } else {
                context.startService(serviceIntent);
            }
        }catch (Exception e){
            Log.d("Error","the service was already running");
        }

        //Add the Upload Receiver with alarmanager
        Intent intent1= new Intent(context, UploadReceiver.class);
        PendingIntent internetIntent = PendingIntent.getBroadcast(context,10,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).
                setRepeating(AlarmManager.RTC_WAKEUP, Utilities.getMilisForAlarm(1000),
                        NOTIFY_INTERNET_INTERVAL, internetIntent);

    }
}
