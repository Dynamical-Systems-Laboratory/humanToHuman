package com.polito.humantohuman.HTTPClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.loopj.android.http.AsyncHttpClient;
import com.polito.humantohuman.ResponseHandler.ResponseHandler;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Class that will be used for all the request to the server
 */
public class HTTPClient {
    /**
     * The context that the asynchttpclient library needs
     */
    private Context context;
    /**
     * The instance of the asynchttpclient
     */
    private AsyncHttpClient asyncHttpClient;
    /**
     * Variable that will setup the content that is been served to the server
     */
    private String contentype = "";
    /**
     * An instance of the ResponseHandler that will be used to handle the response from the server
     */
    private ResponseHandler responseHandler;
    /**
     * The url that will be used to
     */
    private String url ="";
    /**
     * The json values that will be sent
     */
    private String json = "";

    public HTTPClient(ResponseHandler responseHandler,Context context) {
        this.context = context;
        this.asyncHttpClient = new AsyncHttpClient();
        this.responseHandler = responseHandler;
    }

    public void setContentype(String contentype) {this.contentype =contentype;}
    public void setJson(String json) {
        byte ptext[] = json.getBytes();
        try {
            this.json = new String(ptext, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.json = json;}
    public void setUrl(String url) {this.url = url;}

    /**
     * Will make a post request to the server
     * @throws InternetError In case the server is not working
     */
    public void post() throws InternetError {
        if(!isNetworkAvailable()) { throw new InternetError(); }
        try {
            asyncHttpClient.post(context, url, new StringEntity(json), contentype, responseHandler);
        } catch (UnsupportedEncodingException e) {
            throw new InternetError();
        }
    }

    /**
     * Makes get request
     * @throws InternetError
     */
    public void get() throws InternetError{
        if(!isNetworkAvailable()) { throw new InternetError(); }
    }

    public AsyncHttpClient getClient() {return  asyncHttpClient;}


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    //Possible Exceptions in case there is an error with the network. It could be related with the
    //server or with the Internet connection of the device

    public class NetworkError extends  Exception {}
    public class InternetError extends NetworkError{}
    public class ServerError extends NetworkError{}



}
