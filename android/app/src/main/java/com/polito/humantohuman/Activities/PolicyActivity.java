package com.polito.humantohuman.Activities;

import android.content.Context;
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

        //privacyPolText.setText(getString(R.string.privacy_policy));

        privacyPolText.setText(Html.fromHtml(getString(R.string.privacy_policy_check)));
        checkBox.setOnCheckedChangeListener(new OnCheckAcceptedListener());



    }

    private class OnCheckAcceptedListener implements  CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                setPolicyAccepted(PolicyActivity.this,true);
                onPolicyAccepted();
            }
        }
    }

    private void onPolicyAccepted() {
        StartActivity.launchNextActivity(this);
        finish();
    }

    public static boolean isPolicyAccepted(Context context) {
        SharedPreferences sh = (SharedPreferences) context.getSharedPreferences("config",Context.MODE_PRIVATE);
        return sh.getBoolean("privacy",false);
    }

    public static void setPolicyAccepted(Context context, boolean b){
        SharedPreferences sh = (SharedPreferences) context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        editor.putBoolean("privacy",b);
        editor.apply();
    }
}
