package com.polito.humantohuman.Services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;

import com.polito.humantohuman.Database.ConnDatabase;
import com.polito.humantohuman.Receivers.WifiReceiver;

import static com.polito.humantohuman.Constants.TIME.MAX_TIME_UPLOAD;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class UploadJobService extends JobService {

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean startUploadService() {
        super.onCreate();
        ConnDatabase.getInstance(this);
        // First the app checks if there is a wifi connection available
        if(!WifiReceiver.isWifiConnected(this)) {

            //On the first run, the timer is equal to 0, so it changes to the current millis
            if (UploadRunnable.getTimer() == 0) { UploadRunnable.setTimer(System.currentTimeMillis()); return false;}

            //If the timer is not 0, it is checked f it has passed X time after the other time this
            //service was able to run
            if (System.currentTimeMillis() - UploadRunnable.getTimer()  <= MAX_TIME_UPLOAD) { return false; }
        }

        //The task is prepared to be launched
        Handler handler = new Handler();
        handler.post(new UploadRunnable(0,this));
        UploadRunnable.setServiceIsRunning(true);
        return true;
    }
    @Override
    public boolean onStartJob(JobParameters params) {
        super.onCreate();
        return startUploadService();
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
    /**
     * Runnable that will be executed to upload the data
     */
}
