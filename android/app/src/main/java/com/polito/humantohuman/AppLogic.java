package com.polito.humantohuman;

import android.content.Context;
import android.content.Intent;

import static com.polito.humantohuman.utils.Polyfill.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import static com.polito.humantohuman.Database.*;

public class AppLogic {

    public static final int APPSTATE_EXPERIMENT_RUNNING_COLLECTING = 0;
    public static final int APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING = 1;
    public static final int APPSTATE_NO_EXPERIMENT = 2;
    public static final int APPSTATE_LOGGING_IN = 3;
    public static final int APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING = 4;
    public static final int APPSTATE_EXPERIMENT_JOINED_ACCEPTED_NOT_RUNNING = 5;

    private static String serverURL;
    private static int appState;
    private static long bluetoothId;
    private static ArrayList<Database.Row> devices;

    public static void startup(Context context) {
        initializeDatabase(context);
        Server.initializeServer(context);
        Long appStateNullable = getPropNumeric(KEY_APPSTATE);
        long appStateLong = appStateNullable == null ? APPSTATE_NO_EXPERIMENT : appStateNullable;
        appState = (int) appStateLong;

        Server.listener = (response, error) -> {
            if (response != null) {
                System.err.println("got response " + response.toString());
                devices = null;
            }
            if (error != null)
                System.err.println("got error " + error.toString());
        };
        Server.supplier = () -> {
            if (devices == null || devices.isEmpty())
                devices = Database.popRows();
            if (!devices.isEmpty())
                return devices;
            return null;
        };

        Bluetooth.delegate = Database::addRow;

        if (appState != APPSTATE_NO_EXPERIMENT && appState != APPSTATE_LOGGING_IN) {
            serverURL = getPropText(KEY_SERVER_BASE_URL);
            bluetoothId = getPropNumeric(KEY_OWN_ID);

            if (appState == APPSTATE_EXPERIMENT_RUNNING_COLLECTING) {
                context.startService(new Intent(context, Bluetooth.Advertiser.class));
                context.startService(new Intent(context, Bluetooth.Scanner.class));
                context.startService(new Intent(context, Server.class));
            }
        }
    }

    public static int getAppState() {
        return appState;
    }

    public static long getBluetoothID() {
        if (appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN)
            throw new RuntimeException("No id to get!");

        return bluetoothId;
    }

    public static void startCollectingData(Context context) {
        if (appState != APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING)
            throw new RuntimeException("Can't start collecting data while not in an experiment!");

        context.startService(new Intent(context, Bluetooth.Advertiser.class));
        context.startService(new Intent(context, Bluetooth.Scanner.class));
        context.startService(new Intent(context, Server.class));
        appState = APPSTATE_EXPERIMENT_RUNNING_COLLECTING;
        setPropNumeric(KEY_APPSTATE, appState);
    }

    public static void stopCollectingData(Context context) {
        if (appState != APPSTATE_EXPERIMENT_RUNNING_COLLECTING)
            throw new RuntimeException("Can't stop collecting data while not currently collecting!");

        context.stopService(new Intent(context, Bluetooth.Advertiser.class));
        context.stopService(new Intent(context, Bluetooth.Scanner.class));
        context.stopService(new Intent(context, Server.class));
        appState = APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING;
        Database.setPropNumeric(Database.KEY_APPSTATE, appState);
    }

    public static String getServerURL() {
        if (appState == APPSTATE_NO_EXPERIMENT)
            throw new RuntimeException("Can't get server URL when there's no experiment!");
        return serverURL;
    }

    public static String getPrivacyPolicyText() {
        if (appState == APPSTATE_NO_EXPERIMENT)
            return "DEFAULT PRIVACY POLICY: HELLO WORLD!\n";
        return Database.getPropText(Database.KEY_PRIVACY_POLICY);
    }

    public static String getDescriptionText() {
        if (appState == APPSTATE_NO_EXPERIMENT)
            return "DEFAULT DESCRIPTION: HELLO WORLD!\n";
        return Database.getPropText(Database.KEY_EXPERIMENT_DESCRIPTION);
    }

    public static void setServerCredentials(String urlString, Consumer<Exception> cb) {
        if (appState != APPSTATE_NO_EXPERIMENT && appState != APPSTATE_LOGGING_IN)
            throw new RuntimeException("Can't set URL while already in an experiment!");

        try {
            new URL(urlString); // check if the server URL parses
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + urlString);
            cb.accept(e);
            return;
        }

        serverURL = urlString;
        Database.setPropText(Database.KEY_SERVER_BASE_URL, serverURL);
        appState = APPSTATE_LOGGING_IN;
        Database.setPropNumeric(Database.KEY_APPSTATE, appState);

        CountdownExecutor executor = new CountdownExecutor(1, () -> {
            appState = APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING; // TODO change this to be APPSTATE_EXPERIMENT_JOINED_NOT_RUNNING
            Database.setPropNumeric(Database.KEY_APPSTATE, appState);
            System.err.println("login finished successfully");
            cb.accept(null);
        });

        Server.getId((id, error) -> {
            if (error != null) {
                System.err.println("got error: " + error);
                appState = APPSTATE_NO_EXPERIMENT;
                Database.setPropNumeric(Database.KEY_APPSTATE, appState);
                cb.accept(error);
            } else if (id != null) {
                bluetoothId = id;
                Database.setPropNumeric(KEY_OWN_ID, bluetoothId);
                executor.decrement();
            }
        });

        // Server.getPrivacyPolicy
    }
}
