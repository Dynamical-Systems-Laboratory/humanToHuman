package com.polito.humantohuman.Serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.polito.humantohuman.ConnsObjects.BtConn;

import java.lang.reflect.Type;

/**
 * Json serializer for the BtConn Object
 */
public class BtConnSerializer  implements JsonSerializer<BtConn> {
    @Override
    public JsonElement serialize(BtConn src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("rssi", src.getRssi());
        jsonObject.addProperty("time",src.getTime());
        jsonObject.addProperty("device_name",src.getDeviceName());
        jsonObject.addProperty("mac_address",src.getMacAddress());
        jsonObject.addProperty("mobile", src.getMobile());
        jsonObject.addProperty("screen",src.getScreenOn());

        return jsonObject;
    }
}

