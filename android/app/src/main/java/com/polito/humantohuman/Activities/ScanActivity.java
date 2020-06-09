package com.polito.humantohuman.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.polito.humantohuman.R;
import com.polito.humantohuman.Services.Bluetooth;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class will be the core of the application. From here the user can start
 * or stop the service select if he want to upload the data using only a wifi
 * network or not. Also, he can check their anonymous ID.
 */
public class ScanActivity extends AppCompatActivity {

  final class Device {
    public final long id;
    public int powerLevel, rssi;
    public Date lastSeen;

    Device(long id, int powerLevel, int rssi) {
      this.id = id;
      this.powerLevel = powerLevel;
      this.rssi = rssi;
      this.lastSeen = Date.from(Instant.now());
    }
  }

  Switch dataSwitch;
  TableLayout table;
  ArrayList<Device> devices = new ArrayList<>();
  Handler handler =  new Handler();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    dataSwitch = findViewById(R.id.service_running);
    table = findViewById(R.id.deviceRows);

    ScanActivity activity = this;

    Runnable updateTable = new Runnable() {
      @Override
      public void run() {
        table.removeAllViews();
        TableRow header = new TableRow(activity);
        TextView idHeader = new TextView(activity);
        idHeader.setText("Device ID ");
        header.addView(idHeader);
        TextView powerLevelHeader = new TextView(activity);
        powerLevelHeader.setText(" Power Level ");
        header.addView(powerLevelHeader);
        TextView rssiHeader = new TextView(activity);
        rssiHeader.setText(" RSSI ");
        header.addView(rssiHeader);
        table.addView(header);
        synchronized (devices) {
          for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).lastSeen.before(Date.from(Instant.now().minusSeconds(1)))) {
              devices.remove(i);
              i--;
            }
          }
          for (Device device : devices) {
            TableRow tableRow = new TableRow(activity);
            TextView id = new TextView(activity);
            id.setText(device.id + " ");
            tableRow.addView(id);
            TextView powerLevel = new TextView(activity);
            powerLevel.setText(" "+device.powerLevel + " ");
            tableRow.addView(powerLevel);
            TextView rssi = new TextView(activity);
            rssi.setText(" " +device.rssi);
            tableRow.addView(rssi);
            table.addView(tableRow);
          }
        }
        handler.postDelayed(this, 200);
      }
    };

    handler.postDelayed(updateTable, 200);
    dataSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
      if (checked) {
        System.err.println("Starting bluetooth");
        Bluetooth.start((id, powerLevel, rssi) -> {
          synchronized (devices) {
            for (Device device : devices) {
              if (device.id == id) {
                device.powerLevel = powerLevel;
                device.rssi = rssi;
                device.lastSeen = Date.from(Instant.now());
                return;
              }
            }
            devices.add(new Device(id, powerLevel, rssi));
          }
        });
      } else {
        System.err.println("Stopping bluetooth");
        Bluetooth.stop();
      }
    });
  }
}
