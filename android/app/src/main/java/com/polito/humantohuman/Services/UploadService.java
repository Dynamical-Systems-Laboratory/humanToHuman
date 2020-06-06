package com.polito.humantohuman.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.polito.humantohuman.Database.ConnDatabase;
import com.polito.humantohuman.Receivers.WifiReceiver;

import static com.polito.humantohuman.Constants.TIME.MAX_TIME_UPLOAD;

public class UploadService extends Service {



    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onCreate();
        ConnDatabase.getInstance(this);
        if(!WifiReceiver.isWifiConnected(this)) {
            if (UploadRunnable.getTimer() == 0) { UploadRunnable.setTimer(System.currentTimeMillis()); return START_STICKY;}
            if (System.currentTimeMillis() - UploadRunnable.getTimer() <= MAX_TIME_UPLOAD) { return START_STICKY; }
        }

        Handler handler = new Handler();
        handler.post(new UploadRunnable(0,this));
        UploadRunnable.setServiceIsRunning(true);
        ConnDatabase.getInstance(this);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
