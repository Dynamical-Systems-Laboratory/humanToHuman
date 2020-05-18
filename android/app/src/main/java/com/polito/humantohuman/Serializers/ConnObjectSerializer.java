package com.polito.humantohuman.Serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.polito.humantohuman.ConnsObjects.BtConn;
import com.polito.humantohuman.ConnsObjects.ConnObject;
import com.polito.humantohuman.ConnsObjects.WifiConn;

import java.lang.reflect.Type;

/**
 * Serializer for the ConnObject
 */
public class ConnObjectSerializer implements JsonSerializer<ConnObject> {
    @Override
    public JsonElement serialize(ConnObject src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id_device",src.getIdDevice());
        jsonObject.add("bt_conns", context.serialize(src.getBtConns().toArray(), BtConn[].class));
        jsonObject.add("wifi_conns", context.serialize(src.getWifiConns().toArray(), WifiConn[].class));
        try {
            JsonElement loc = context.serialize(src.getLocationConn());
            jsonObject.add("loc_conn", loc);
        } catch (NullPointerException e) { }
        return jsonObject;
    }
}
