package com.polito.humantohuman.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.polito.humantohuman.Constants;
import com.polito.humantohuman.DataController;
import com.polito.humantohuman.Listeners.StateChangeListener;
import com.polito.humantohuman.R;
import com.polito.humantohuman.Receivers.BtReceiver;
import com.polito.humantohuman.Services.BGScanService;
import com.polito.humantohuman.Utilities;

import static com.polito.humantohuman.Constants.TIME.NOTIFY_INTERNET_INTERVAL;

/**
 * This class will be the core of the application. From here the user can start or stop the service
 * select if he want to upload the data using only a wifi network or not.
 * Also, he can check their anonymous ID.
 */
public class ScanActivity extends AppCompatActivity implements StateChangeListener {


   // private Button checkButton;
    private AlarmManager alarmMgr;
    private PendingIntent internetIntent;
    private TextView htmlTextView;
//    private TextView running;

    private Switch service_switch;
    private Switch wifi_switch;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        overridePendingTransition(R.anim.slide_in,R.anim.slide_out);
        //Adding view
        //checkButton = findViewById(R.id.check_items);
        service_switch = findViewById(R.id.service_running);
        wifi_switch = findViewById(R.id.wifi);
        htmlTextView = findViewById(R.id.html_text);

        wifi_switch.setOnCheckedChangeListener(new onWifiSwitchListener());
        service_switch.setOnCheckedChangeListener(new onServiceSwitchListener());
        htmlTextView.setText(Html.fromHtml(getString(R.string.app_text)));

        //Set the anonymous id of the device
        TextView textView = findViewById(R.id.md5_id);
        String md5 = Utilities.convertPassMd5(Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID)).substring(0,16);
        textView.setText(getString(R.string.anonymous_id, Utilities.convertPassMd5(md5)));


        //running = findViewById(R.id.running_text);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                int i = usageStatsManager.getAppStandbyBucket();
                Log.d("Status:", "Battery bucket: " + i);

            }
        }*/
        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        //Google auth stuff
        DataController.getInstance().addScanFinishedListener(this);
        setStatus();
        //Add buttons functionality
      /*  checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDatabase();
            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!Constants.Permissions.checkPermissions(this)) {
            Constants.Permissions.requestPermissions(this);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BtReceiver.setOriginalName(this);
    }


    public void changeServiceStatus() {
        if(BGScanService.getServiceStatus() == Constants.STATUS.RUNNING) {
            stopService();
        } else {
            startService();
        }
    }

    //https://developer.android.com/topic/libraries/architecture/workmanager/
    public void startService(){
        if(BGScanService.getServiceStatus() == Constants.STATUS.RUNNING) {return;}
        Intent serviceIntent = new Intent(this, BGScanService.class);
        serviceIntent.setAction(Constants.SERVICE_ACTION.START_FOREGROUND_INTENT);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            this.startForegroundService(serviceIntent);
        } else {
            this.startService(serviceIntent);
        }
    }

    private void stopService(){
        if(BGScanService.getServiceStatus() == Constants.STATUS.RUNNING) {
            //DataController.getInstance().stopScan(this);
            Log.d("Status", "Trying to stop de service");
            Intent stopIntent = new Intent(this, BGScanService.class);
            stopIntent.setAction(Constants.SERVICE_ACTION.STOP_FOREGROUND_INTENT);
            startService(stopIntent);
            BtReceiver.setOriginalName(this);
        }
    }

    @Override
    public void onStateChanged() {
        setStatus();
    }

    public void setStatus(){
        boolean state = BGScanService.getServiceStatus() == Constants.STATUS.RUNNING;
        service_switch.setChecked(state);
    }

    public class onServiceSwitchListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                startService();
            } else {
                stopService();
            }
        }
    }

    public class onWifiSwitchListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences sh = ScanActivity.this.getSharedPreferences("config", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sh.edit();
            editor.putBoolean("wifi",isChecked);
            editor.apply();
        }
    }
}
