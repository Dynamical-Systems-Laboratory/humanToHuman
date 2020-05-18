package com.polito.humantohuman.HTTPClient;

import android.content.Context;
import android.util.Log;

import com.polito.humantohuman.AuthUser;
import com.polito.humantohuman.ResponseHandler.ResponseHandler;
import com.polito.humantohuman.Utilities;


public class HTTPClientBuilder {
    private static String JSON_HEADER = "application/json";
    private final Context context;

    private HTTPClient httpClient;

    public HTTPClientBuilder (Context context, ResponseHandler responseHandler) {
        this.context = context;
        this.httpClient = new HTTPClient(responseHandler,context); }

    public HTTPClientBuilder addHeader(String header,String value) {
        httpClient.getClient().addHeader(header,value);
        return this;
    }
    public HTTPClientBuilder setContenType(String contenType) {
        httpClient.setContentype(contenType);
        return this;
    }

    /**
     * Method that add the JSON header to the HTTP petition
     * @param json
     * @return
     */
    public HTTPClientBuilder setJson(String json) {httpClient.setJson(json);
    return this; }

    /**
     * It adds to the petition header the authorization parameters needed on the server
     * @return
     */
    public HTTPClientBuilder addAuth() {
        try {
            addHeader("token", AuthUser.getInstance(context).getCurrentToken());
            addHeader("uid", AuthUser.getInstance(context).getUid());
            addHeader("id", Utilities.getSecureId(context));

        } catch (NullPointerException exception) {
            Log.d("Error", "You must sign in to the app");
        }
        return this;
    }

    /**
     * Set the time out in millis of a connection
     * @param time Milliseconds
     * @return
     */
    public HTTPClientBuilder setTimeOut(int time) {httpClient.getClient().setTimeout(time); return this;}

    /**
     * Set the response timeout of the server
     * @param time Milliseconds
     * @return
     */
    public HTTPClientBuilder setResponseTimeOut(int time) { httpClient.getClient().setResponseTimeout(time); return this;}

    /**
     * Set the maximum retries of a petition with the timeout
     * @param retries
     * @param timeout Milliseconds
     * @return
     */
    public HTTPClientBuilder setRetriesAndTimeout(int retries, int timeout) { httpClient.getClient()
            .setMaxRetriesAndTimeout(retries,timeout);
            return this;
    }

    /**
     * Set the specific url where we want to make the petition
     * @param url
     * @return
     */
    public HTTPClientBuilder setUrl(String url) {httpClient.setUrl(url); return this;}

    /**
     * Set the Json header
     * @return
     */
    public HTTPClientBuilder setJsonHeader() {setContenType(JSON_HEADER); return this;}

    /**
     * Build de HTTPClient object
     * @return
     */
    public HTTPClient build() { return httpClient;}


}
