package com.polito.humantohuman.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.polito.humantohuman.Constants;
import com.polito.humantohuman.R;

/**
 * Activity used for asking the user the necessary permissions to use the app
 * In a case where the user doesn't want to accept the permission, he will have the change to
 * request them again. Otherwise he won't pass from this activity.
 */
public class PermissionActivity extends AppCompatActivity {

    private Button requestPermissions;
    private Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        overridePendingTransition(R.anim.slide_in,R.anim.slide_out);
        if(checkForPermissions()) {
            permissionsGranted();
        } else {
            requestPermissions();
        }

        requestPermissions = findViewById(R.id.request_permissions);
        next = findViewById(R.id.next);

        requestPermissions.setOnClickListener(new onRequestClick());
        next.setOnClickListener(new onNextClick());
    }

    private boolean checkForPermissions() {
        return Constants.Permissions.checkPermissions(this);
    }

    private void requestPermissions() {
        Constants.Permissions.requestPermissions(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Constants.Permissions.setPermissions(this);
        if(checkForPermissions()) {
            permissionsGranted();
        } else {
            permissionsNotGranted();
        }
    }

    public void permissionsGranted() {
        StartActivity.launchNextActivity(this);
        finish();
    }

    public void permissionsNotGranted(){
        Toast.makeText(this,"You must accept all the permissions", Toast.LENGTH_LONG).show();
    }

    private class onRequestClick implements View.OnClickListener {

        @Override
        public void onClick(View v) { requestPermissions(); }
    }

    private class onNextClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(checkForPermissions()){
                permissionsGranted();
            } else {
                permissionsNotGranted();
            }
        }
    }
}
