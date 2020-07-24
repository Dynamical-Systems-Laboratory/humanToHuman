package com.polito.humantohuman.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import com.polito.humantohuman.AppLogic;
import com.polito.humantohuman.R;

public class SettingsActivity extends AppCompatActivity {

  Button exitButton;
  Button setServerButton;
  Button privacyPolicyButton;
  TextView setServerEditText;
  Button leaveExperimentButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    exitButton = findViewById(R.id.settingsExitButton);
    exitButton.setOnClickListener((view) -> this.finish());
    setServerEditText = findViewById(R.id.settingsSetServerEditText);
    setServerButton = findViewById(R.id.settingsSetServerButton);
    privacyPolicyButton = findViewById(R.id.settingsPrivacyPolicyButton);
    leaveExperimentButton = findViewById(R.id.settingsLeaveExperimentButton);

//        setServerEditText.setText(
//                "http://192.168.10.102:8080/experiment/albert-debug");

    privacyPolicyButton.setOnClickListener((view) -> {
      Intent intent = new Intent(this, PolicyActivity.class);
      startActivity(intent);
    });

    setServerButton.setOnClickListener((view) -> {
      setServerButton.setEnabled(false);
      AppLogic.setServerCredentials(
          setServerEditText.getText().toString(), (error) -> {
            if (error != null) {
              setServerButton.setEnabled(true);
            } else {
              Intent intent = new Intent(this, PolicyActivity.class);
              startActivity(intent);
              finish();
            }
          });
    });

    leaveExperimentButton.setOnClickListener((view) -> {
      AppLogic.leaveExperiment(this);
      this.onResume();
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    switch (AppLogic.getAppState()) {
    case AppLogic.APPSTATE_NO_EXPERIMENT:
      setServerButton.setEnabled(true);
      privacyPolicyButton.setEnabled(false);
      leaveExperimentButton.setEnabled(false);
      break;
    case AppLogic.APPSTATE_LOGGING_IN:
      setServerButton.setEnabled(false);
      privacyPolicyButton.setEnabled(false);
      leaveExperimentButton.setEnabled(true);
      break;
    default:
      setServerButton.setEnabled(false);
      privacyPolicyButton.setEnabled(true);
      leaveExperimentButton.setEnabled(true);
    }
  }
}
