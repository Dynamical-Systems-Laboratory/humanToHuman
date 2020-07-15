package com.polito.humantohuman;

import static com.polito.humantohuman.Database.*;
import static com.polito.humantohuman.utils.Polyfill.*;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AppLogic {

  public static final int APPSTATE_EXPERIMENT_RUNNING_COLLECTING = 0;
  public static final int APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING = 1;
  public static final int APPSTATE_NO_EXPERIMENT = 2;
  public static final int APPSTATE_LOGGING_IN = 3;
  public static final int APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING =
      4;
  public static final int APPSTATE_EXPERIMENT_JOINED_ACCEPTED_NOT_RUNNING = 5;

  private static String serverURL;
  private static int appState;
  private static long bluetoothId;
  private static boolean onlyWifi;
  private static ArrayList<Database.Row> devices;
  private static WifiManager wifiManager;
  private static String token;

  public static void startup(Context context) {
    initializeDatabase(context);
    Server.initializeServer(context);
    wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    Long onlyWifiNullable = getPropNumeric(KEY_ONLY_WIFI);
    onlyWifi = onlyWifiNullable != null && onlyWifiNullable == 1;

    Long appStateNullable = getPropNumeric(KEY_APPSTATE);
    long appStateLong =
        appStateNullable == null ? APPSTATE_NO_EXPERIMENT : appStateNullable;
    appState = (int)appStateLong;

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
        devices = popRows();
      if (!devices.isEmpty())
        return devices;
      return null;
    };
    Bluetooth.delegate = Database::addRow;

    if (appState != APPSTATE_NO_EXPERIMENT) {
      serverURL = getPropText(KEY_SERVER_BASE_URL);
      if (appState != APPSTATE_LOGGING_IN) {
        bluetoothId = getPropNumeric(KEY_OWN_ID);
        token = getPropText(KEY_TOKEN);
      }

      if (appState == APPSTATE_EXPERIMENT_RUNNING_COLLECTING) {
        context.startService(new Intent(context, Bluetooth.class));
        context.startService(new Intent(context, Server.class));
      }
    }
  }

  public static int getAppState() { return appState; }

  private static void setAppState(int state) {
    appState = state;
    setPropNumeric(KEY_APPSTATE, appState);
  }

  public static boolean shouldUpload() {
    return !onlyWifi || isWifiConnected();
  }

  public static boolean getOnlyWifi() {
    return onlyWifi;
  }

  public static void setOnlyWifi(boolean value) {
    onlyWifi = value;
    setPropNumeric(KEY_ONLY_WIFI, onlyWifi ? 1 : 0);
  }

  public static long getBluetoothID() {
    if (appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN)
      throw new RuntimeException("No id to get!");

    return bluetoothId;
  }

  public static String getToken() {
    if (appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN)
      throw new RuntimeException("We don't have a token to give!");
    return token;
  }


  public static void startCollectingData(Context context) {
    if (appState != APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING)
      throw new RuntimeException(
          "Can't start collecting data while not in an experiment!");

    context.startService(new Intent(context, Bluetooth.class));
    context.startService(new Intent(context, Server.class));
    setAppState(APPSTATE_EXPERIMENT_RUNNING_COLLECTING);
  }

  public static void stopCollectingData(Context context) {
    if (appState != APPSTATE_EXPERIMENT_RUNNING_COLLECTING)
      throw new RuntimeException(
          "Can't stop collecting data while not currently collecting!");

    context.stopService(new Intent(context, Bluetooth.class));
    context.stopService(new Intent(context, Server.class));
    setAppState(APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING);
  }

  public static String getServerURL() {
    if (appState == APPSTATE_NO_EXPERIMENT)
      throw new RuntimeException(
          "Can't get server URL when there's no experiment!");
    return serverURL;
  }

  public static String getPrivacyPolicyText() {
    if (appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN)
      throw new RuntimeException("We don't have a privacy policy to give!");
    return getPropText(KEY_PRIVACY_POLICY);
  }

  public static String getDescriptionText(Context ctx) {
    if (appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN)
      return ctx.getString(R.string.app_text);
    return getPropText(KEY_EXPERIMENT_DESCRIPTION);
  }

  public static void setServerCredentials(String urlString,
                                          Consumer<Exception> cb) {
    if (appState != APPSTATE_NO_EXPERIMENT)
      throw new RuntimeException(
          "Can't set URL while already in an experiment!");

    try {
      new URL(urlString); // check if the server URL parses
    } catch (MalformedURLException e) {
      System.err.println("Malformed URL: " + urlString);
      cb.accept(e);
      return;
    }

    serverURL = urlString;
    setPropText(KEY_SERVER_BASE_URL, serverURL);
    setAppState(APPSTATE_LOGGING_IN);

    CountdownExecutor executor = new CountdownExecutor(1, () -> {
      setAppState(APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING);
      cb.accept(null);
    });

    RunOnceExecutor<Exception> errorExecutor =
        new RunOnceExecutor<>((error) -> {
          System.err.println("got error: " + error);
          setAppState(APPSTATE_NO_EXPERIMENT);
          cb.accept(error);
        });

    Server.getDescription((description, error) -> {
      if (error != null) {
        errorExecutor.run(error);
      } else if (description != null) {
        setPropText(KEY_EXPERIMENT_DESCRIPTION, description);
        executor.decrement();
      }
    });

    Server.getPrivacyPolicy((policy, error) -> {
      if (error != null) {
        errorExecutor.run(error);
      } else if (policy != null) {
        setPropText(KEY_PRIVACY_POLICY, policy);
        executor.decrement();
      }
    });
  }

  public static void acceptPrivacyPolicy(Consumer<Exception> cb) {
    Server.getId((id, tok, error) -> {
      if (error != null) {
        System.err.println("got error: " + error);
        setAppState(APPSTATE_NO_EXPERIMENT);
        cb.accept(error);
      } else if (id != null) {
        bluetoothId = id;
        setPropNumeric(KEY_OWN_ID, bluetoothId);
        token = tok;
        setPropText(KEY_TOKEN, token);
        setAppState(APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING);
        cb.accept(null);
      }
    });
  }

  public static void rejectPrivacyPolicy(Context context) {
    if (appState == APPSTATE_LOGGING_IN || appState == APPSTATE_NO_EXPERIMENT)
      throw new RuntimeException("no privacy policy to reject!");

    if (appState == APPSTATE_EXPERIMENT_RUNNING_COLLECTING) {
      context.stopService(new Intent(context, Bluetooth.class));
      context.stopService(new Intent(context, Server.class));
    }

    setAppState(APPSTATE_NO_EXPERIMENT);
  }

  public static void ignorePrivacyPolicy() {
    if (appState != APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING)
      throw new RuntimeException("use rejectPrivacyPolicy instead");

    setAppState(APPSTATE_NO_EXPERIMENT);

  }

  /**
   * Check if the wifi is connected or not
   * @return whether or not wifi is connected
   */
  public static boolean isWifiConnected() {
    if(wifiManager.isWifiEnabled()) {
      WifiInfo wifiInfo = wifiManager.getConnectionInfo();
      return wifiInfo.getNetworkId() != -1;
    }
    return false;
  }
}
