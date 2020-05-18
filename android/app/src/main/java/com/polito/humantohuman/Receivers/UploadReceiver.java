package com.polito.humantohuman.Receivers;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.polito.humantohuman.Database.ConnDatabase;
import com.polito.humantohuman.Services.UploadJobService;
import com.polito.humantohuman.Services.UploadRunnable;
import com.polito.humantohuman.Services.UploadService;
import com.polito.humantohuman.Utilities;


/**
 * Receiver that will handle the job or service that will be used to upload the stored data.
 */
public class UploadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Utilities.initializeApp(context);

        //First check that the user wants to upload the data only using wifi
        if(!WifiReceiver.isWifiConnected(context) && WifiReceiver.isUploadByWifiEnable(context)){
            Log.d("Status","Only upload Data with wifi mode");
            return;
        }
        Log.d("Alarm","The process to upload the data has been called");
        Log.d("Status", "Items saved: " + ConnDatabase.getInstance(context).rows());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (!UploadRunnable.SERVICE_IS_RUNNING) {
                    uploadJob(context);
                }
            } else {
                if (!UploadRunnable.SERVICE_IS_RUNNING) {
                    context.startService(new Intent(context, UploadService.class));
                }
            }
        } catch (IllegalStateException e){
            Log.d("Error", "Job was already running" );
        }
    }

    /**
     * In case that the Android device, has a version over Android M (included), it will be used a JobService
     * instead of using a service.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void uploadJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo job = createJob(context);
        try {
            jobScheduler.schedule(job);
        } catch (Exception e) {
            context.startService(new Intent(context,UploadService.class));
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void cancelJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        try {
            jobScheduler.cancel(createJob(context).getId());
        }catch (Exception e) {
            Log.d("Error", "Job was not running");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static JobInfo createJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, UploadJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(2, serviceComponent);
        builder.setMinimumLatency(15 * 1000); // wait at least
        //builder.setOverrideDeadline(5 * 1000); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false);// we don't care if the device is charging or not
        return  builder.build();
    }


}

