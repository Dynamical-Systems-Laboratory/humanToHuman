package com.polito.humantohuman.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.polito.humantohuman.R;

public class PolicyActivity extends AppCompatActivity {

    private CheckBox checkBox;
    private TextView privacyPolText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        overridePendingTransition(R.anim.slide_in,R.anim.slide_out);

        checkBox = findViewById(R.id.agree_checkbox);
        privacyPolText = findViewById(R.id.agree_policy_text);

        privacyPolText.setText(Html.fromHtml(getString(R.string.privacy_policy_check)));
        checkBox.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                Intent intent = new Intent(this, ScanActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
