package com.polito.humantohuman.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
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
import static com.polito.humantohuman.Constants.TIME.MAX_TIME_UPLOAD;
import static com.polito.humantohuman.Constants.TIME.RESPONSE_TIME_OUT;
import static com.polito.humantohuman.Constants.TIME.SEND_SAVE_DATA_THREAD_TIME;
import static com.polito.humantohuman.Constants.TIME.TIME_OUT;

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
