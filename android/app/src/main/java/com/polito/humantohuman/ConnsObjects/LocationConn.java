package com.polito.humantohuman.ConnsObjects;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

import com.polito.humantohuman.Constants;

import io.realm.RealmObject;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.polito.humantohuman.Constants.Permissions.checkPermission;

public class LocationConn extends RealmObject {

    public static long LAST_SCAN = 0;
    private double latitude;
    private double longitude;
    private long time;
    private double altitude;
    private float accuracy;

    public LocationConn(Location location){
        this.latitude= location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
        this.accuracy = location.getAccuracy();
        this.time = location.getTime();
    }
    public LocationConn() {}


    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public long getTime() { return time; }

    public void setTime(long time) { this.time = time; }

    public double getAltitude() { return altitude; }

    public void setAltitude(double altitude) { this.altitude = altitude; }

    public float getAccuracy() { return accuracy; }

    public void setAccuracy(float accuracy) { this.accuracy = accuracy; }

    public static LocationConn getLocation(Context context) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            LocationProvider locationProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
            long currentMilis = System.currentTimeMillis();

            /**
             * How much time does it need to pass to get the localization
             */
            if(LAST_SCAN != 0 && (currentMilis - LAST_SCAN ) < Constants.TIME.MAX_TIME_LOCALIZATION) {return null;}

            if(checkPermission(context,ACCESS_COARSE_LOCATION) || checkPermission(context,ACCESS_FINE_LOCATION)){
                LAST_SCAN = currentMilis;
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                //Return Null to disable
                return new LocationConn(location);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
