package com.polito.humantohuman.Database;

import android.content.Context;

import com.polito.humantohuman.ConnsObjects.ConnObject;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ConnDatabase {
    /**
     * Instance of the realm database
     */
    private Realm realm;
    /**
     * Current version of the Database
     */
    private static final int DB_VERSION =3 ;
    /**
     * Database name
     */
    public static final String DB_NAME ="db";
    /**
     * Instance of the migration class
     */
    private static final MigrationDatabase migrationDatabase = new MigrationDatabase();
    /**
     * Everything related with the Singleton Instance
     */
    private static ConnDatabase instance;
    public static ConnDatabase getInstance() {
        if(instance == null) {
            instance = new ConnDatabase();
        }
        return instance;
    }

    public static ConnDatabase getInstance(Context context){
        if(instance == null) {
            Realm.init(context);
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .schemaVersion(DB_VERSION)
                    .name(DB_NAME)
                    .migration(migrationDatabase)
                    .build();
            Realm.setDefaultConfiguration(config);

        }
        return getInstance();
    }
    private ConnDatabase() {
        this.realm = Realm.getDefaultInstance(); }

    /**
     * Method to insert new scanned data
     * @param connObject All the data that has been scanned during the scan
     */
    public void insertData(ConnObject connObject){
        realm.beginTransaction();
        realm.insertOrUpdate(connObject);
        realm.commitTransaction();
    }

    /**
     * Get first connObject stored in the database
     * @return
     */
    public ConnObject getFirst() {
        int id = realm.where(ConnObject.class).min("id").intValue();
        return realm.copyFromRealm(realm.where(ConnObject.class).equalTo("id",id).findFirst());
    }

    /**
     * Get last object of the database
     * @return
     */
    public ConnObject getLast() {
        int id = realm.where(ConnObject.class).max("id").intValue();
        return realm.copyFromRealm(realm.where(ConnObject.class).equalTo("id",id).findFirst());
    }

    /**
     * Remove first object of the database
     */
    public void removeFirst() {
        realm.beginTransaction();
        realm.where(ConnObject.class).findAll().deleteFirstFromRealm();
        realm.commitTransaction();
    }

    /**
     * Remove last object of the database
     */
    public void removeLast() {
        realm.beginTransaction();
        realm.where(ConnObject.class).findAll().deleteLastFromRealm();
        realm.commitTransaction();
    }

    /**
     * Remove an object of the database, depending of the id
     * @param id
     */
    public void removeWithId(int id){
        realm.beginTransaction();
        realm.where(ConnObject.class).equalTo("id", id).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    /**
     * Get the number of rows of the database
     * @return
     */
    public int rows() { return (int) realm.where(ConnObject.class).count();}

    /**
     * Check if the database is empty
     * @return
     */
    public boolean isEmpty() { return rows() == 0; }

    /**
     * Import the database as a list
     * @return
     */
    public List<ConnObject> getList() {return realm.copyFromRealm( realm.where(ConnObject.class).findAll());}

    /**
     * Get the id of the last object of the database
     * @return
     */
    public int getLastId() {
        try {
            return realm.where(ConnObject.class).max("id").intValue();
        } catch (Exception e) {
            return  0;
        }
    }

}
