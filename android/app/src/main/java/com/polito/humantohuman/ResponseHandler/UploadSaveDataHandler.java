package com.polito.humantohuman.ResponseHandler;

import android.content.Context;
import android.os.Build;

import com.polito.humantohuman.ConnsObjects.ConnObject;
import com.polito.humantohuman.Database.ConnDatabase;
import com.polito.humantohuman.Services.UploadJobService;
import com.polito.humantohuman.Services.UploadRunnable;
import com.polito.humantohuman.Services.UploadService;

import org.json.JSONObject;


/**
 * This Handler is used when the device is capable of uploading local data
 */
public class UploadSaveDataHandler extends ResponseHandler {
    /**
     * The object that must be removed from the database
     */
    private final ConnObject connObject;
    public UploadSaveDataHandler(ConnObject connObject, Context context) {
        super(context);
        this.connObject = connObject;
    }

    @Override
    public void onGoodResponse(int statusCode, JSONObject responseBody) {
        ConnDatabase.getInstance(context).removeWithId((int) connObject.getId());
        UploadRunnable.setIsSendingData(false);

    }

    @Override
    public void onBadResponse(int statusCode, JSONObject errorResponse) {
        //The data should be stored so we shouldn't make anything
        UploadRunnable.setServiceIsRunning(false);
        UploadRunnable.setIsSendingData(false);
    }

}
