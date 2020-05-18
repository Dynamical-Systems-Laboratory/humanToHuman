package com.polito.humantohuman.Serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.polito.humantohuman.ConnsObjects.LocationConn;

import java.lang.reflect.Type;

public class LocationConnSerializer implements JsonSerializer<LocationConn> {
    @Override
    public JsonElement serialize(LocationConn src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("latitude",src.getLatitude());
        jsonObject.addProperty("longitude",src.getLongitude());
        jsonObject.addProperty("altitude",src.getAltitude());
        jsonObject.addProperty("time",src.getTime());
        jsonObject.addProperty("accuracy",src.getAccuracy());
        return  jsonObject;
    }
}
