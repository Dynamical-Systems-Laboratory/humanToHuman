package com.polito.humantohuman;

import static com.polito.humantohuman.Database.*;
import static com.polito.humantohuman.utils.Polyfill.*;

import android.Manifest;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
  private static long noise;
  private static Date lastModifiedTime;
  private static boolean serverServiceIsRunning = false;

  public static boolean startup(Context context) {
    initializeDatabase(context);
    Server.initializeServer(context);
    wifiManager = (WifiManager)context.getApplicationContext().getSystemService(
        Context.WIFI_SERVICE);
    Long onlyWifiNullable = getPropNumeric(KEY_ONLY_WIFI);
    onlyWifi = onlyWifiNullable != null && onlyWifiNullable == 1;
    Long noiseNullable = getPropNumeric(KEY_NOISE);
    noise = noiseNullable == null ? 0 : noiseNullable;
    lastModifiedTime = Calendar.getInstance().getTime();

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

    if (appState == APPSTATE_NO_EXPERIMENT) {
      return true;
    }

    serverURL = getPropText(KEY_SERVER_BASE_URL);
    if (appState != APPSTATE_LOGGING_IN) {
      bluetoothId = getPropNumeric(KEY_OWN_ID);
      token = getPropText(KEY_TOKEN);
    }

    if (appState == APPSTATE_EXPERIMENT_RUNNING_COLLECTING) {
      if (!Bluetooth.isEnabled())
        return false;
      context.startService(new Intent(context, Bluetooth.class));
      context.startService(new Intent(context, Server.class));
      serverServiceIsRunning = true;
    }

    return true;
  }

  public static int getAppState() { return appState; }

  public static long getNoise() {
    long n = noise;
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, -1);
    if (lastModifiedTime.before(calendar.getTime())) {
      noise += 1;
      setPropNumeric(KEY_NOISE, noise);
    }

    return n;
  }

  public static void resetNoise() {
    noise = 0;
    setPropNumeric(KEY_NOISE, noise);
  }

  private static void setAppState(int state) {
    appState = state;
    setPropNumeric(KEY_APPSTATE, appState);
  }

  public static boolean shouldUpload() {
    if (!onlyWifi)
      return true;
    if (wifiManager.isWifiEnabled())
      return wifiManager.getConnectionInfo().getNetworkId() != -1;
    return false;
  }

  public static boolean getOnlyWifi() { return onlyWifi; }

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

  public static boolean startCollectingData(Context context) {
    if (appState != APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING)
      throw new RuntimeException(
          "Can't start collecting data while not in an experiment!");

    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED) {
      return false;
    }

    if (!Bluetooth.isEnabled())
      return false;
    context.startService(new Intent(context, Bluetooth.class));
    if (!serverServiceIsRunning)
      context.startService(new Intent(context, Server.class));
    serverServiceIsRunning = true;
    setAppState(APPSTATE_EXPERIMENT_RUNNING_COLLECTING);
    return true;
  }

  public static void stopCollectingData(Context context) {
    if (appState != APPSTATE_EXPERIMENT_RUNNING_COLLECTING)
      throw new RuntimeException(
          "Can't stop collecting data while not currently collecting!");

    context.stopService(new Intent(context, Bluetooth.class));
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

    CountdownExecutor executor = new CountdownExecutor(2, () -> {
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
    }

    if (serverServiceIsRunning) {
      context.stopService(new Intent(context, Server.class));
      serverServiceIsRunning = false;
    }

    popRows();
    devices = null;

    setAppState(APPSTATE_NO_EXPERIMENT);
  }

  public static void leaveExperiment(Context ctx) {
    if (appState != APPSTATE_EXPERIMENT_RUNNING_COLLECTING &&
        appState != APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING &&
        appState != APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING) {
      throw new RuntimeException("use privacy policy-related methods instead");
    }

    if (appState == APPSTATE_EXPERIMENT_RUNNING_COLLECTING) {
      ctx.stopService(new Intent(ctx, Bluetooth.class));
    }

    popRows();
    devices = null;

    if (serverServiceIsRunning) {
      ctx.stopService(new Intent(ctx, Server.class));
      serverServiceIsRunning = false;
    }

    setAppState(APPSTATE_NO_EXPERIMENT);
  }
}
