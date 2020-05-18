package com.polito.humantohuman.ResponseHandler;

import android.content.Context;

import com.polito.humantohuman.ConnsObjects.ConnObject;
import com.polito.humantohuman.Database.ConnDatabase;

import org.json.JSONObject;

/**
 * This Handler is used when a scan has finished and the device is capable of uploading the information
 */
public class UploadDataHandler extends ResponseHandler {
    /**
     * The object that must be removed from the database
     */
    private final ConnObject connObject;
    public UploadDataHandler(ConnObject connObject, Context context) {
        super(context);
        this.connObject = connObject;
    }
    @Override
    public void onGoodResponse(int statusCode, JSONObject responseBody) {
        ConnDatabase.getInstance(context).removeWithId((int) connObject.getId());
    }

    @Override
    public void onBadResponse(int statusCode, JSONObject errorResponse) {
        //The data should be stored so we shouldn't make anything
    }

    @Override
    public void on400(JSONObject errorResponse) {
        super.on400(errorResponse);
        ConnDatabase.getInstance(context).removeWithId((int) connObject.getId());
    }
}
