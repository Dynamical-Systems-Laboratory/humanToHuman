package com.polito.humantohuman.Activities;

import android.content.*;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.widget.*;
import com.polito.humantohuman.*;
import static com.polito.humantohuman.AppLogic.*;
import com.polito.humantohuman.utils.Polyfill;

import java.util.*;

/**
 * This class will be the core of the application. From here the user can start
 * or stop the service select if he want to upload the data using only a wifi
 * network or not. Also, he can check their anonymous ID.
 */
public final class ScanActivity extends AppCompatActivity {

  final class Device {
    public final long id;
    public int powerLevel, rssi;
    public Date lastSeen;

    Device(long id, int powerLevel, int rssi) {
      this.id = id;
      this.powerLevel = powerLevel;
      this.rssi = rssi;
      this.lastSeen = new Date();
    }
  }

  Switch scanSwitch;
  Button settingsButton;
  TextView experimentDescription;
  boolean checked = false;

  ArrayList<Database.Row> rows;
  ArrayList<Device> devices = new ArrayList<>();
  Handler handler = new Handler();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    scanSwitch = findViewById(R.id.service_running);
    settingsButton = findViewById(R.id.scanSettingsButton);
    experimentDescription = findViewById(R.id.scanExperimentDescription);

    AppLogic.startup(this);

    switch (getAppState()) {
      case APPSTATE_EXPERIMENT_RUNNING_COLLECTING:
        scanSwitch.setEnabled(true);
        scanSwitch.setChecked(true);
        break;
      case APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING:
        scanSwitch.setEnabled(true);
        scanSwitch.setChecked(false);
        break;
      default:
        scanSwitch.setEnabled(false);
        scanSwitch.setChecked(false);
    }

    scanSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
      this.checked = checked;
      if (checked) {
        System.err.println("Starting bluetooth");
        startCollectingData(this);
      } else {
        System.err.println("Stopping bluetooth");
        stopCollectingData(this);
      }
    });

    settingsButton.setOnClickListener((view) -> {
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
    });

    experimentDescription.setText(getDescriptionText());
  }

  @Override
  protected void onResume() {
    super.onResume();
    switch (getAppState()) {
      case APPSTATE_EXPERIMENT_RUNNING_COLLECTING:
        scanSwitch.setEnabled(true);
        scanSwitch.setChecked(true);
        break;
      case APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING:
        scanSwitch.setEnabled(true);
        scanSwitch.setChecked(false);
        break;
      default:
        scanSwitch.setEnabled(false);
        scanSwitch.setChecked(false);
    }
  }
}
