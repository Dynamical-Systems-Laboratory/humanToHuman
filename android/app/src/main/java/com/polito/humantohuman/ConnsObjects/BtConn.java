package com.polito.humantohuman.ConnsObjects;

//An object structure, to save the information of a BT connection

import com.polito.humantohuman.Receivers.ScreenReceiver;
import com.polito.humantohuman.Utilities;

import java.text.Normalizer;

import io.realm.RealmObject;


public class BtConn extends RealmObject {
    /**
     * Name of the found device
     */
    private String deviceName;
    /**
     * RSSI value of the found device
     */
    private int rssi;
    /**
     * Timestamp of the found device
     */
    private long time;
    /**
     * Mac address of the found device
     */
    private String macAddress;
    /**
     * If the found device is an smart phone or not
     */
    private boolean mobile = false;
    /**
     * If the actual device had the screen on, when it a device was found
     */
    private boolean screenOn;

    public BtConn(){}
    /**
     * Class used to store all the devices that have been scanned by this device
     * @param deviceName
     * @param rssi
     * @param macAddress
     * @param mobile
     * @param time
     */
    public BtConn(String deviceName,int rssi, String macAddress,boolean mobile,long time) {
        this.deviceName = mobile ?  deviceName : Utilities.convertPassMd5(deviceName);
        this.rssi = rssi;
        this.time = time;
        this.macAddress = macAddress;
        this.mobile = mobile;
        this.screenOn = ScreenReceiver.isScreenOn();
    }
    public BtConn(String deviceName, int rssi, String macAddress, boolean mobile) {
        this(deviceName, rssi, macAddress,mobile,System.currentTimeMillis());
    }

    public boolean getMobile() { return mobile; }

    public String getDeviceName() {
        return Normalizer.normalize(deviceName, Normalizer.Form.NFKD);
    }

    public int getRssi() {
        return rssi;
    }

    public long getTime() { return time; }

    public String getMacAddress() { return macAddress;}

    public boolean getScreenOn() { return screenOn; }

    public void setMobile(boolean mobile) { this.mobile = mobile; }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setScreenOn(boolean screenOn) { this.screenOn = screenOn; }
}
