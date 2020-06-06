package com.polito.humantohuman.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.polito.humantohuman.Constants;
import com.polito.humantohuman.R;
import com.polito.humantohuman.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity will be loaded first, when the app is started.
 * First it will check which activities should be opened and then it aggregate those into an static list.
 */
public class StartActivity extends AppCompatActivity {
    public static final List<Activity> activities = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        overridePendingTransition(R.anim.slide_in,R.anim.slide_out);
        Utilities.initializeApp(this);
        /**
         *  First we check that all the permissions has been accepted. Otherwise the permission activity
         *  will be fired
         */
        if(!Constants.Permissions.checkPermissions(this)){
            activities.add(new PermissionActivity ()); }

        /**
         * We need to check that the user has accepted the policy
         */
        if(!PolicyActivity.isPolicyAccepted(this)) {
            activities.add(new PolicyActivity());
        }

        /**
         * Add the scan activity
         */
        activities.add(new ScanActivity());
        launchNextActivity(this, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
    }

    /**
     * This method is used to launch the first activity of the list with custom flags
     * @param currentActivity activity from where we are calling the method
     * @param flags
     */
    public static void launchNextActivity(Activity currentActivity, int flags) {
        Activity activity = activities.get(0);
        activities.remove(0);
        Intent intent = new Intent(currentActivity,activity.getClass());
        intent.addFlags(flags);
        currentActivity.startActivity(intent);
    }

    /**
     * This method is used to launch the first activity of the list
     * @param currentActivity activity from where we are calling the method
     */
    public static void launchNextActivity(Activity currentActivity) {
        Activity activity = activities.get(0);
        activities.remove(0);
        Intent intent = new Intent(currentActivity,activity.getClass());
        currentActivity.startActivity(intent);
    }
}
