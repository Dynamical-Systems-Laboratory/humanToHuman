package com.polito.humantohuman.Serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.polito.humantohuman.ConnsObjects.WifiConn;

import java.lang.reflect.Type;

/**
 * Serializer for the WifiConn object
 */
public class WifiConnSerializer implements JsonSerializer<WifiConn> {
    @Override
    public JsonElement serialize(WifiConn src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("rssi", src.getRssi());
        jsonObject.addProperty("time",src.getTime());
        jsonObject.addProperty("bssid",src.getBssid());
        return jsonObject;
    }
}
