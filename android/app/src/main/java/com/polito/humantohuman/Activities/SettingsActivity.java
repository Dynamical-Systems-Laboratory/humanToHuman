package com.polito.humantohuman.Activities;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        exitButton = findViewById(R.id.settingsExitButton);
        exitButton.setOnClickListener((view) -> this.finish());
        setServerEditText = findViewById(R.id.settingsSetServerEditText);
        setServerButton = findViewById(R.id.settingsSetServerButton);
        setServerButton.setOnClickListener((view) -> {
            setServerEditText.setText("");
            setServerButton.setEnabled(false);

            AppLogic.setServerURL(setServerEditText.getText().toString(), (succeeded) -> {
                System.err.println("hello world: "+succeeded);
                if (!succeeded) {
                    setServerButton.setEnabled(true);
                }
            });
        });

    }
}
