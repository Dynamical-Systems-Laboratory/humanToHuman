package com.polito.humantohuman.Activities;

import static com.polito.humantohuman.OverflowAreaUtils.*;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.polito.humantohuman.R;
import java.util.*;

/**
 * This class will be the core of the application. From here the user can start
 * or stop the service select if he want to upload the data using only a wifi
 * network or not. Also, he can check their anonymous ID.
 */
public class ScanActivity extends AppCompatActivity {
  BluetoothAdapter adapter;

  static ArrayList<UUID> getUUIDs(byte[] bytes) {
    ArrayList<UUID> uuids = new ArrayList<>();
    for (int i = 0; i < bytes.length; i++) {
      int len = bytes[i];
      if (len == 0)
        break;
      int end = len + i;
      if (len < 3) {
        i = end;
        continue;
      }

      if (bytes[++i] == 6) { // We're in an incomplete UUID list
        if (len != 17) {
          i = end;
          continue;
        } // It's not following the apple behavior

        long first = 0, second = 0;
        for (int j = 0; j < 8; j++)
          first = (first << 8) | bytes[++i];
        for (int j = 0; j < 8; j++)
          second = (second << 8) | bytes[++i];
        uuids.add(
            new UUID(Long.reverseBytes(second), Long.reverseBytes(first)));
      }

      if (bytes[i] != -1) {
        i = end;
        continue;
      }
      if (bytes[++i] != 76) {
        i = end;
        continue;
      }
      if (bytes[++i] != 0) {
        i = end;
        continue;
      }
      if (bytes[++i] != 1) {
        i = end;
        continue;
      }
      for (int j = 0; i < end; j++) {
        byte current = bytes[++i];
        for (int k = 0; k < 8; k++) {
          int mask = 1 << (7 - k);
          if ((current & mask) != 0) {
            uuids.add(SERVICE_UUIDS[8 * j + k]);
          }
        }
      }
    }
    return uuids;
  }

  static Long getID(ArrayList<UUID> uuids) {
    long id = 0;
    boolean foundSentinel = false;
    for (UUID uuid : uuids) {
      int index = SERVICE_UUIDS_TO_BITS.get(uuid);
      if (index == 0) {
        foundSentinel = true;
        continue;
      }
      id |= ((long)1) << (index - 8);
    }
    if (foundSentinel)
      return id;
    return null;
  }

  public static byte getReversedByte(long value, int index) {
    byte byteValue = (byte) ((value >> (index * 8)) & ~-256);
    byte y=0;
    for(int position=7; position>0; position--){
      y+=((byteValue&1)<<position);
      byteValue >>= 1;
    }
    return y;

  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);

    adapter = BluetoothAdapter.getDefaultAdapter();

    adapter.startLeScan((device, rssi, scanRecord) -> {
      Long id = getID(getUUIDs(scanRecord));
      if (id != null)
        System.out.println(id);
    });

    long id = 120_000_000;

    byte[] advertisementData = new byte[] {
        1, -1 << 7,
            getReversedByte(id, 0), getReversedByte(id, 1),
            getReversedByte(id, 2), getReversedByte(id, 3),
            getReversedByte(id, 4), getReversedByte(id, 5),
            getReversedByte(id, 6), getReversedByte(id, 7),
            0, 0, 0, 0, 0, 0, 0
    };

    AdvertisingSetParameters params = new AdvertisingSetParameters.Builder()
            .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
            .setConnectable(false)
            .build();
    AdvertiseSettings settings = new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .build();
    AdvertiseData data =
        new AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .addManufacturerData(0x4C, advertisementData).build();

    BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
    advertiser.startAdvertising(settings, data, new AdvCallback());
  }


  static class AdvCallback extends AdvertiseCallback {
    public AdvCallback() {}
  }
}
