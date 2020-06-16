package com.polito.humantohuman.Activities;

import android.content.*;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.widget.*;
import com.polito.humantohuman.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class will be the core of the application. From here the user can start
 * or stop the service select if he want to upload the data using only a wifi
 * network or not. Also, he can check their anonymous ID.
 */
public final class ScanActivity extends AppCompatActivity {

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

  Database database;
  Switch scanSwitch;
  ArrayList<Database.Row> rows;
  TableLayout table;
  ArrayList<Device> devices = new ArrayList<>();
  Handler handler = new Handler();
  boolean checked = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    Server.initializeServer(this);
    database = new Database(this);
    scanSwitch = findViewById(R.id.service_running);
    table = findViewById(R.id.deviceRows);

    scanSwitch.setChecked(checked);
    Bluetooth.id = 5952679123360942499L;

    ScanActivity activity = this;
    Runnable updateTable = new Runnable() {
      @Override
      public void run() {
        table.removeAllViews();
        table.addView(createTableRow(activity, "Device ID", "Power Level", "RSSI"));
        synchronized (devices) {
          Date secondAgo = Date.from(Instant.now().minusSeconds(1));
          List<Device> l = devices.stream()
                  .filter(e -> e.lastSeen.after(secondAgo))
                  .map(d -> {
                    table.addView(createTableRow(activity, ""+d.id, ""+d.powerLevel, ""+d.rssi));
                    return d;
                  })
                  .collect(Collectors.toList());
          devices.clear();
          devices.addAll(l);
        }
        handler.postDelayed(this, 200);
      }
    };
    handler.postDelayed(updateTable, 200);

    Server.supplier = () -> {
      if (rows == null || rows.isEmpty())
        rows = database.popRows();
      if (!rows.isEmpty())
        return rows;
      return null;
    };

    Server.initializeServer(this);
    Server.listener = (response, error) -> {
      if (response != null)
        System.err.println("got response " + response.toString());
      if (error != null)
        System.err.println("got error " + error.toString());
      rows = null;
    };

    Bluetooth.delegate = (id, powerLevel, rssi) -> {
      synchronized (devices) {
        database.addRow(id, powerLevel, rssi);
        for (Device device : devices) {
          if (device.id == id) {
            device.powerLevel = powerLevel;
            device.rssi = rssi;
            device.lastSeen = Date.from(Instant.now());
            return;
          }
        }
        devices.add(new Device(id, powerLevel, rssi));
      }; // Semicolon here is because of bug in clang-format
    };

    scanSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
      this.checked = checked;
      if (checked) {
        System.err.println("Starting bluetooth");
        startService(new Intent(this, Bluetooth.Advertiser.class));
        startService(new Intent(this, Bluetooth.Scanner.class));
        startService(new Intent(this, Server.class));
      } else {
        System.err.println("Stopping bluetooth");
        stopService(new Intent(this, Bluetooth.Advertiser.class));
        stopService(new Intent(this, Bluetooth.Scanner.class));
        stopService(new Intent(this, Server.class));
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    scanSwitch.setChecked(checked);
  }

  static TableRow createTableRow(Context ctx, String id, String powerLevel, String rssi) {
    TableRow tableRow = new TableRow(ctx);
    TextView tid = new TextView(ctx);
    tid.setText(id + " ");
    tableRow.addView(tid);
    TextView tpowerLevel = new TextView(ctx);
    tpowerLevel.setText(" " + powerLevel + " ");
    tableRow.addView(tpowerLevel);
    TextView trssi = new TextView(ctx);
    trssi.setText(" " + rssi);
    tableRow.addView(trssi);
    return tableRow;
  }
}
