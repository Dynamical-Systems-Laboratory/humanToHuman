package com.polito.humantohuman.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.polito.humantohuman.Activities.StartActivity;
import com.polito.humantohuman.DataController;
import com.polito.humantohuman.R;
import com.polito.humantohuman.Receivers.BtReceiver;
import com.polito.humantohuman.Receivers.RestartServiceReceiver;
import com.polito.humantohuman.Utilities;

import java.util.TimerTask;

import static com.polito.humantohuman.Constants.MAX_BATTERY;
import static com.polito.humantohuman.Constants.SERVICE_ACTION;
import static com.polito.humantohuman.Constants.STATUS.NOT_RUNNING;
import static com.polito.humantohuman.Constants.STATUS.RUNNING;
import static com.polito.humantohuman.Constants.TIME;
import static com.polito.humantohuman.Constants.TIME.NOTIFY_INTERVAL;
import static com.polito.humantohuman.Utilities.getBatteryPercentage;

public class BGScanService extends Service {
    /**
     * Instance of the DataController
     */
    public DataController dataController;
    /**
     * Value that will return the current state of the service
     */
    private static int SERVICE_STATUS = NOT_RUNNING;
    /**
     * Instant of when has the last scan started
     */
    public static long LAST_SCAN = 0;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utilities.initializeApp(this);

        dataController = DataController.getInstance();
        String action = "";
        try { action = intent.getAction(); } catch (Exception e) {
            Log.d("Error", "Action not provided");
        }
        try { action.equals(""); } catch (NullPointerException e) { return START_STICKY; }
        //When the user manually stops the service
        if(action.equals(SERVICE_ACTION.STOP_FOREGROUND_INTENT)) { stopService(); }


        //Check that the battery is not lower than the threshold
        if(getBatteryPercentage(this) <= MAX_BATTERY){return START_STICKY;}



        //When the service (or the scan) must be restarted
        if(action.equals(SERVICE_ACTION.RESTART_FOREGROUND_INTENT)) { restartService(); }

        //When the user manually press the start button
        if(action.equals(SERVICE_ACTION.START_FOREGROUND_INTENT) ) { startService(); }

        //When the device has been restarted or power on, we want the service to be up
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)) { startService(); }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //while(dataController.isScanning()){}
        SERVICE_STATUS = NOT_RUNNING;
        stopForeground(true);
        Log.d("Status", "Service has been destroyed");

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Method that will restart the service
     */
    private void restartService() {
        SERVICE_STATUS = RUNNING;
        new Handler().post(new RepeatTask(false));
    }

    /**
     * How to start the service, depending of the Android version. In the case of Android Oreo and newer
     * versions the service will be started with the startMyOwnForeground Method, otherwise with the
     * startForeground
     */
    private void startService() {
        SERVICE_STATUS = RUNNING;
        //Stick to the main thread
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //A different way to start the foreground service, one for versions before android Oreo
            //a notification should appear!
            startMyOwnForeground();
        } else {
            startForeground(1, new Notification());
        }


        new Handler().post(new RepeatTask());
    }

    /**
     * If we need to stop the service
     */
    private void stopService() {
        SERVICE_STATUS = NOT_RUNNING;
        dataController.stopScan(this);
        try {
            BtReceiver.setOriginalName(this);
        } catch (Exception e) {}
        stopForeground(true);
        stopSelf();
        onDestroy();
    }

    /**
     * Set the notification that Android asks for the foreground service
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "rssiscan";
        String channelName = "Performing an Scan";
        /**
         * Creating Notification Channel
         */
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_UNSPECIFIED);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        RemoteViews collapsedView = new RemoteViews(getPackageName(),R.layout.not_collapsed);

        /**
         * Since it crash sometimes with UNSPECIFIED, we should try with None.
         */

        try{
            manager.createNotificationChannel(chan);
            Log.d("Status","Starting the service with UNSPECIFIED importance" );

        } catch (IllegalArgumentException e) {
            chan.setImportance(NotificationManager.IMPORTANCE_NONE);
            manager.createNotificationChannel(chan);
            Log.d("Status","Starting the service with NONE importance" );
        }

        //Setting onclick on the notification
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setPriority(NotificationManager.IMPORTANCE_UNSPECIFIED)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setCustomContentView(collapsedView)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("HTH is scanning"))

               //         .bigText(getString(R.string.performing_scan)))
                .build();
        startForeground(2, notification);
    }

    public static synchronized int getServiceStatus() {return  SERVICE_STATUS;}
    public static synchronized void setServiceStatus(int status) {SERVICE_STATUS = status;}
    public static synchronized long getLastScanTime() {return LAST_SCAN;}
    public static synchronized void setLastScanTime(long lastScanTime) {LAST_SCAN = lastScanTime;}

    /**
     * Task that will be repeated for every scan that is going to be performed, in the case where the
     * background service is running
     */
    public class RepeatTask extends TimerTask {
        private final boolean setPostDelayed;
        private RepeatTask(boolean setPostDelayed) {this.setPostDelayed = setPostDelayed; }
        private RepeatTask() {this(true); }
        @Override
        public void run() {
            //Check that the backgrounservice is active
            if(getServiceStatus() != NOT_RUNNING) {

                long currentMilis = System.currentTimeMillis();
                setLastScanTime(currentMilis);


                Intent i = new Intent(BGScanService.this, RestartServiceReceiver.class);
                i.putExtra("last_scan",currentMilis);
                i.setAction("restart_service");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(BGScanService.this
                        ,1,i,PendingIntent.FLAG_UPDATE_CURRENT);


                DataController.getInstance().startScan(BGScanService.this);
                //This will happen if the task is executed from another handler or when the service
                //has been started

                if(setPostDelayed){
                    new Handler().postDelayed(new RepeatTask(),NOTIFY_INTERVAL);
                }


                Utilities.setAlarm(BGScanService.this,pendingIntent,TIME.RESTART_TIME_SERVICE);


            }
        }
    }



}
