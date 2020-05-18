package com.polito.humantohuman.ResponseHandler;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.google.gson.JsonObject;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.polito.humantohuman.AuthUser;
import com.polito.humantohuman.HTTPClient.HTTPClient;
import com.polito.humantohuman.HTTPClient.HTTPClientBuilder;
import com.polito.humantohuman.R;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.polito.humantohuman.Constants.SERVER_ENDPOINT.ID_DEVICE_ENDPOINT;
import static com.polito.humantohuman.Constants.TIME.RESPONSE_TIME_OUT;
import static com.polito.humantohuman.Constants.TIME.TIME_OUT;

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

        switch (statusCode){
            case ERROR_CODES.DATA_NOT_VALID: { on422(errorResponse); break; }
            case ERROR_CODES.UNAUTHORIZED_ACTION: { on401(errorResponse); break; }
            case ERROR_CODES.DEVICE_DOES_NOT_EXIST: { on410(errorResponse); break; }
            case ERROR_CODES.DATABASE_ERROR: { on500(errorResponse); break; }
            case ERROR_CODES.TOKEN_NOT_VALID: {on402(errorResponse); break; }
            case ERROR_CODES.BAD_REQUEST: {on400(errorResponse); break;}
        }
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
     * When the device does not exist, we sent the device id.
     * @param errorResponse
     */
    public void on410 (JSONObject errorResponse) {
        HTTPClient httpClient = new HTTPClientBuilder(context,new AddDeviceHandler(context))
                .addAuth()
                .setJsonHeader()
                .setResponseTimeOut(RESPONSE_TIME_OUT)
                .setTimeOut(RESPONSE_TIME_OUT)
                .setRetriesAndTimeout(1, TIME_OUT)
                .setUrl(ID_DEVICE_ENDPOINT)
                .build();
        try {
            httpClient.post();
        } catch (HTTPClient.InternetError internetError) {
            internetError.printStackTrace();
        }
        Log.d("Error" , errorResponse.toString());
    }

    /**
     * Server errors
     * @param errorResponse
     */
    public void on500 (JSONObject errorResponse) { Log.d("Error" , errorResponse.toString()); }

    public void on402 (JSONObject errorResponse) {
        try {
            AuthUser.getInstance(context).refreshToken();
        } catch (Exception e) {

        }
    }

    /**
     * The data is not compatible with the server
     * @param errorResponse
     */
    public void on400 (JSONObject errorResponse){};
}
