package com.polito.humantohuman.ConnsObjects;

import io.realm.RealmObject;

public class WifiConn extends RealmObject {

    /**
     * RSSI value of the found device
     */
    private int rssi;
    /**
     * Mac address of the found device
     */
    private String bssid;
    /**
     * Timestamp of the found device
     */
    private long time;

    public WifiConn(){}
    public WifiConn(String bssid, int rssi, long time) {
        this.rssi = rssi;
        this.bssid = bssid;
        this.time = time;
    }

    public WifiConn(String bssid, int rssi) {
        this(bssid,rssi,System.currentTimeMillis());
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getRssi() { return rssi; }
    public String getBssid() { return bssid; }
    public long getTime() { return time; }
}
