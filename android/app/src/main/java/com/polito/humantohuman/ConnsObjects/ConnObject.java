package com.polito.humantohuman.ConnsObjects;


import android.location.Location;

import com.google.gson.annotations.Expose;
import com.polito.humantohuman.Database.ConnDatabase;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Class that will store all the wifiConns and btConns of every scan.
 */
public class ConnObject extends RealmObject {
    /**
     * The anonymous id of the current device
     */
    private String idDevice;

    private LocationConn locationConn;
    /**
     * A list with the current bt devices that the device has found
     */
    private RealmList<BtConn> btConns;
    /**
     * A list with the current wifi beacons that the device has found
     */
    private RealmList<WifiConn> wifiConns;

    /**
     * Id to be saved in the database
     */
    @PrimaryKey @Index @Expose
    private  long id;

    public ConnObject(RealmList<BtConn> btConns,RealmList<WifiConn> wifiConns,LocationConn locationConn, String idDevice){
        this.btConns = btConns;
        this.wifiConns =wifiConns;
        this.idDevice = idDevice;
        this.locationConn = locationConn;
        this.id = ConnDatabase.getInstance().getLastId() +1 ;
    }

    public ConnObject(RealmList<BtConn> btConns,RealmList<WifiConn> wifiConns, LocationConn locationConn) {
        this.btConns = btConns;
        this.wifiConns = wifiConns;
        this.locationConn = locationConn;
        this.id = ConnDatabase.getInstance().getLastId() +1 ;
    }

    //For future implementations
    public ConnObject () { this(new RealmList<BtConn>(), new RealmList<WifiConn>(),null); }

    public String getIdDevice() { return idDevice; }

    public RealmList<BtConn> getBtConns() { return btConns; }

    public RealmList<WifiConn> getWifiConns() { return wifiConns; }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public void setIdDevice(String idDevice) { this.idDevice = idDevice;}

    public void setBtConns(RealmList<BtConn> btConns) {
        this.btConns = btConns;
    }

    public void setWifiConns(RealmList<WifiConn> wifiConns) {
        this.wifiConns = wifiConns;
    }

    public LocationConn getLocationConn() {
        return locationConn;
    }

    public void setLocationConn(LocationConn locationConn) {
        this.locationConn = locationConn;
    }

    public void clearData(){
        btConns.clear();
        wifiConns.clear();
    }
    //Method that will add information saved from the database
    public void addOtherData(ConnObject connObject){
        this.btConns.addAll(connObject.btConns);
        this.wifiConns.addAll(connObject.wifiConns);
        this.locationConn = connObject.getLocationConn();
    }

    public boolean isEmpty() {
        return btConns.isEmpty() && wifiConns.isEmpty();
    }

    public ConnObject getCopy() {
        ConnObject connObject = new ConnObject();
        connObject.setIdDevice(this.idDevice);
        connObject.addOtherData(this);
        connObject.setId(this.id);
        return  connObject;
    }

}
