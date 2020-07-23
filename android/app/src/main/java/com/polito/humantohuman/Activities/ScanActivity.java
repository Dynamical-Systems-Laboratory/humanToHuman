package com.polito.humantohuman.Activities;

import static com.polito.humantohuman.AppLogic.*;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.*;
import com.polito.humantohuman.*;
import com.polito.humantohuman.utils.PermissionUtils;
import com.polito.humantohuman.utils.Polyfill;
import java.util.*;

/**
 * This class will be the core of the application. From here the user can start
 * or stop the service select if he want to upload the data using only a wifi
 * network or not. Also, he can check their anonymous ID.
 */
public final class ScanActivity extends AppCompatActivity {

  Switch scanSwitch;
  Switch onlyWifiSwitch;
  Button settingsButton;
  TextView anonymousId;
  TextView experimentDescription;

  CompoundButton.OnCheckedChangeListener scanSwitchListener;
  CompoundButton.OnCheckedChangeListener onlyWifiSwitchListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    scanSwitch = findViewById(R.id.service_running);
    settingsButton = findViewById(R.id.scanSettingsButton);
    experimentDescription = findViewById(R.id.scanExperimentDescription);
    onlyWifiSwitch = findViewById(R.id.wifi);
    anonymousId = findViewById(R.id.scanAnonymousId);

    ScanActivity self = this;
    scanSwitchListener = new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView,
                                   boolean isChecked) {
        if (isChecked) {
          System.err.println("Starting bluetooth");
          if (startCollectingData(self)) {
            return;
          }
          self.scanSwitch.setOnCheckedChangeListener(null);
          buttonView.setChecked(false);
          self.scanSwitch.setOnCheckedChangeListener(this);

          if (ContextCompat.checkSelfPermission(
                  self, Manifest.permission.ACCESS_FINE_LOCATION) ==
              PackageManager.PERMISSION_GRANTED) {
            Intent enableBtIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
            // Bluetooth is off
            return;
          }

          PermissionUtils.requestPermission(
              self, 1, Manifest.permission.ACCESS_FINE_LOCATION, false);
        } else {
          System.err.println("Stopping bluetooth");
          stopCollectingData(self);
        }
      }
    };

    AppLogic.startup(this);
    scanSwitch.setOnCheckedChangeListener(scanSwitchListener);

    settingsButton.setOnClickListener((view) -> {
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
    });

    onlyWifiSwitchListener = (buttonView, checked) -> { setOnlyWifi(checked); };
    onlyWifiSwitch.setOnCheckedChangeListener(onlyWifiSwitchListener);
  }

  @Override
  protected void onResume() {
    super.onResume();

    experimentDescription.setText(Html.fromHtml(getDescriptionText(this)));

    onlyWifiSwitch.setOnCheckedChangeListener(null);
    onlyWifiSwitch.setChecked(getOnlyWifi());
    onlyWifiSwitch.setOnCheckedChangeListener(onlyWifiSwitchListener);

    scanSwitch.setOnCheckedChangeListener(null);
    System.err.println("appstate is: " + getAppState());
    switch (getAppState()) {
    case APPSTATE_NO_EXPERIMENT:
      scanSwitch.setEnabled(false);
      scanSwitch.setChecked(false);
      anonymousId.setText("ID: No ID yet");
      break;
    case APPSTATE_EXPERIMENT_RUNNING_COLLECTING:
      scanSwitch.setEnabled(true);
      scanSwitch.setChecked(true);
      System.err.println(AppLogic.getBluetoothID());
      anonymousId.setText(
          String.format(Locale.ENGLISH, "ID: %d", AppLogic.getBluetoothID()));
      break;
    case APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING:
      scanSwitch.setEnabled(true);
      scanSwitch.setChecked(false);
      anonymousId.setText(
          String.format(Locale.ENGLISH, "ID: %d", AppLogic.getBluetoothID()));
      break;
    case APPSTATE_LOGGING_IN:
    case APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING:
      scanSwitch.setEnabled(false);
      scanSwitch.setChecked(false);
      break;
    case APPSTATE_EXPERIMENT_JOINED_ACCEPTED_NOT_RUNNING:
      scanSwitch.setEnabled(false);
      scanSwitch.setChecked(false);
      anonymousId.setText(
          String.format(Locale.ENGLISH, "ID: %d", AppLogic.getBluetoothID()));
      break;
    default:
      throw new RuntimeException("Unknown state");
    }
    scanSwitch.setOnCheckedChangeListener(scanSwitchListener);
  }
}
