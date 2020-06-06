package com.polito.humantohuman.ResponseHandler;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.polito.humantohuman.R;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

//This is a test class only for posting info
public abstract class ResponseHandler extends JsonHttpResponseHandler {
    /**
     * Context, from when the application have requested the HTTP petition
     */
    protected final Context context;

    /**
     * Possible error codes from the server
     */
    private class ERROR_CODES{
        public static final int DATA_NOT_VALID = 422;
        public static final int UNAUTHORIZED_ACTION = 401;
        public static final int TOKEN_NOT_VALID = 402;
        public static final int DEVICE_DOES_NOT_EXIST = 410;
        public static final int DATABASE_ERROR = 500;
        public static final int BAD_REQUEST = 400;
    }

    public ResponseHandler(Context context) {
        this.context = context;
    }

    @CallSuper
    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
        //Toast.makeText(context, "Data has been sent", Toast.LENGTH_LONG).show();
        Log.d("Status: ",context.getString(R.string.data_has_send));
        onGoodResponse(statusCode,responseBody);

    }
    @CallSuper
    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        //Toast.makeText(context, "Data hasn't been sent", Toast.LENGTH_LONG).show();
        Log.d("Error: ",context.getString(R.string.data_hasn_send_server));
        onBadResponse(statusCode,errorResponse);
    }

    /**
     * What to do with a good response
     * @param statusCode
     * @param responseBody
     */
    public abstract void onGoodResponse(int statusCode, JSONObject responseBody);

    /**
     * What to do in a bad response
     * @param statusCode
     * @param errorResponse
     */
    public abstract void onBadResponse(int statusCode, JSONObject errorResponse);

    //Right now they make the same, but in case we have to do something special in one of them
    //they are divided
    /**
     *  When the data is not valid
     */
    public void on401 (JSONObject errorResponse) { Log.d("Error" , errorResponse.toString()); }

    /**
     * When the device is not authorized to make this action
     * @param errorResponse
     */
    public void on422 (JSONObject errorResponse) { Log.d("Error" , errorResponse.toString()); }


    /**
     * Server errors
     * @param errorResponse
     */
    public void on500 (JSONObject errorResponse) { Log.d("Error" , errorResponse.toString()); }

    /**
     * The data is not compatible with the server
     * @param errorResponse
     */
    public void on400 (JSONObject errorResponse){};
}
