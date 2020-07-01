package com.polito.humantohuman.Activities;

import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import com.polito.humantohuman.AppLogic;
import com.polito.humantohuman.R;

public class SettingsActivity extends AppCompatActivity {

  Button exitButton;
  Button setServerButton;
  TextView setServerEditText;
  TextView serverErrorText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    exitButton = findViewById(R.id.settingsExitButton);
    exitButton.setOnClickListener((view) -> this.finish());
    setServerEditText = findViewById(R.id.settingsSetServerEditText);
    setServerButton = findViewById(R.id.settingsSetServerButton);
    serverErrorText = findViewById(R.id.settingsServerErrorText);

    setServerEditText.setText("http://192.168.1.151:8080/experiment/password");

    setServerButton.setEnabled(AppLogic.getAppState() ==
                               AppLogic.APPSTATE_NO_EXPERIMENT);
    setServerButton.setOnClickListener((view) -> {
      setServerButton.setEnabled(false);
      AppLogic.setServerCredentials(
          setServerEditText.getText().toString(), (error) -> {
            if (error != null) {
              setServerButton.setEnabled(true);
              serverErrorText.setText(error.toString());
            } else {
              serverErrorText.setText("SUCCESS!");
            }
          });
    });
  }
}
