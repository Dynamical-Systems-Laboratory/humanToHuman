package com.polito.humantohuman;

import android.content.Context;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Server {

    public interface Listener {
        void onFinish(JSONObject array, VolleyError error);
    }

    public RequestQueue requestQueue;
    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    public Server(Context ctx) {
        requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.start();
    }

    public JSONObject serializeRows(ArrayList<Database.Row> rows, long id) {
        try {
            JSONArray jsonArray  = new JSONArray();
            for (Database.Row row : rows) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("other", row.id);
                jsonObject.put("time", format.format(row.date));
                jsonObject.put("power", row.power);
                jsonObject.put("rssi", row.rssi);
                jsonArray.put(jsonObject);
            }
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("id", id);
            jsonObject.put("connections", jsonArray);
            return jsonObject;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

     public void sendData(ArrayList<Database.Row> rows, long id, Listener listener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://192.168.1.151:8080/addConnections",
                serializeRows(rows, id),
                (response) -> listener.onFinish(response, null),
                (error) -> listener.onFinish(null, error));

        requestQueue.add(request);
     }
}
