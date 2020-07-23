package com.polito.humantohuman.Activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.TextView;
import com.polito.humantohuman.AppLogic;
import com.polito.humantohuman.R;

public class PolicyActivity extends AppCompatActivity {

  private CheckBox checkBox;
  private TextView privacyPolText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_policy);
    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    checkBox = findViewById(R.id.agree_checkbox);
    privacyPolText = findViewById(R.id.privacyPolicyText);

    checkBox.setOnCheckedChangeListener((view, isChecked) -> {
      if (isChecked) {
        AppLogic.acceptPrivacyPolicy((error) -> {
          if (error != null) {
            System.err.println("Got error while accepting privacy policy: " +
                               error);
          } else {
            this.finish();
          }
        });
      } else {
        new AlertDialog.Builder(this)
            .setTitle("Revoke Consent Form")
            .setMessage("You will be kicked from the experiment.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes,
                               (dialog, whichButton) -> {
                                 AppLogic.rejectPrivacyPolicy(this);
                                 finish();
                               })
            .setNegativeButton(
                android.R.string.no,
                (dialog, whichButton) -> checkBox.setChecked(true))
            .show();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    switch (AppLogic.getAppState()) {
    case AppLogic.APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING:
    case AppLogic.APPSTATE_NO_EXPERIMENT:
    case AppLogic.APPSTATE_LOGGING_IN:
      checkBox.setChecked(false);
      break;
    default:
      checkBox.setChecked(true);
    }

    privacyPolText.setText(AppLogic.getPrivacyPolicyText());
  }
}
