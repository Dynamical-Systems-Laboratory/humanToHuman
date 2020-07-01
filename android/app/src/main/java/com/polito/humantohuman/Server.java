package com.polito.humantohuman;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.polito.humantohuman.utils.Polyfill;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Server extends Service {
  private static final String SEND_CHANNEL_ID = "HumanToHumanSending";
  private static final String SEND_CHANNEL_NAME = "HumanToHuman Sending";
  private static final int SEND_FOREGROUND_ID = 3;
  public static Polyfill.Supplier<ArrayList<Database.Row>> supplier;
  public static Listener<JSONObject> listener;
  public static final SimpleDateFormat format =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
  public static RequestQueue requestQueue;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Polyfill.startForeground(this, SEND_FOREGROUND_ID, SEND_CHANNEL_ID,
                               SEND_CHANNEL_NAME);
    } else {
      startForeground(SEND_FOREGROUND_ID, new Notification());
    }

    Handler handler = new Handler();
    Runnable runner = new Runnable() {
      @Override
      public void run() {
        ArrayList<Database.Row> rows = supplier.get();
        if (rows == null) {
          handler.postDelayed(this, 3000);
          return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST, AppLogic.getServerURL() + "/addConnections",
            serializeRows(rows),
            (response)
                -> {
              listener.onFinish(response, null);
              handler.postDelayed(this, 3000);
            },
            (error) -> {
              listener.onFinish(null, error);
              handler.postDelayed(this, 3000);
            });

        requestQueue.add(request);
      }
    };
    handler.postDelayed(runner, 3000);

    return Service.START_STICKY;
  }

  public interface Listener<T> { void onFinish(T data, VolleyError error); }

  public static void initializeServer(Context ctx) {
    if (requestQueue != null)
      return;
    requestQueue = Volley.newRequestQueue(ctx);
    requestQueue.start();
  }

  public static JSONObject serializeRows(ArrayList<Database.Row> rows) {
    try {
      JSONArray jsonArray = new JSONArray();
      for (Database.Row row : rows) {
        jsonArray.put(new JSONObject()
                          .put("other", row.id)
                          .put("time", format.format(row.date))
                          .put("power", row.power)
                          .put("rssi", row.rssi));
      }

      return new JSONObject()
          .put("id", AppLogic.getBluetoothID())
          .put("connections", jsonArray);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  public static void getId(Listener<Long> l) {
    System.err.println("Server url: " + AppLogic.getServerURL());
    JsonObjectRequest req = new JsonObjectRequest(
        Request.Method.POST, AppLogic.getServerURL() + "/addUser",
        new JSONObject(), (response) -> {
          try {
            l.onFinish(response.getLong("id"), null);
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }, (error) -> l.onFinish(null, error));

    requestQueue.add(req);
  }
}
