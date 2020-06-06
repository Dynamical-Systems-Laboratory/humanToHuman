package com.polito.humantohuman.ResponseHandler;

import android.content.Context;

import com.polito.humantohuman.Listeners.AddedDeviceListener;

import org.json.JSONObject;

/**
 * Handler used after requesting to the server to register the device
 */

public class AddDeviceHandler extends ResponseHandler {
    /**
     * Listener that will receive an event when the device receive a response from the server.
     */
    private final AddedDeviceListener listener;
    public AddDeviceHandler(Context context, AddedDeviceListener listener) {
        super(context);
        this.listener = listener;
    }

    public AddDeviceHandler(Context context) {this(context,null);}

    @Override
    public void onGoodResponse(int statusCode, JSONObject responseBody) {
        if(listener  != null) { listener.onAddedDeviceListener(true); };
    }

    @Override
    public void onBadResponse(int statusCode, JSONObject errorResponse) {
        if(listener  != null) { listener.onAddedDeviceListener(false); };
    }
}
