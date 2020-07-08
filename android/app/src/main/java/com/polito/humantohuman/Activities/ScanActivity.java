package com.polito.humantohuman.Activities;

import static com.polito.humantohuman.AppLogic.*;

import android.content.*;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.widget.*;
import com.polito.humantohuman.*;
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
  TextView experimentDescription;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    scanSwitch = findViewById(R.id.service_running);
    settingsButton = findViewById(R.id.scanSettingsButton);
    experimentDescription = findViewById(R.id.scanExperimentDescription);
    onlyWifiSwitch = findViewById(R.id.wifi);

    AppLogic.startup(this);

    scanSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
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

    onlyWifiSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
      setOnlyWifi(checked);
    });

    onlyWifiSwitch.setChecked(getOnlyWifi());

    experimentDescription.setText(getDescriptionText());
    switch (getAppState()) {
      case APPSTATE_NO_EXPERIMENT:
        scanSwitch.setEnabled(false);
        scanSwitch.setChecked(false);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        break;
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

  @Override
  protected void onResume() {
    super.onResume();

    experimentDescription.setText(getDescriptionText());
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
