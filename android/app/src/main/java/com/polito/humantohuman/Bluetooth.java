package com.polito.humantohuman;

import static com.polito.humantohuman.utils.OverflowAreaUtils.*;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.polito.humantohuman.utils.Polyfill;
import java.util.*;

public final class Bluetooth extends Service {
  public interface BluetoothDelegate {
    void foundDevice(long id, int power, int rssi);
  }

  public static BluetoothDelegate delegate = null;

  public static final String SCAN_CHANNEL_ID = "HumanToHumanScanning";
  public static final String SCAN_CHANNEL_NAME = "HumanToHuman Scanning";
  public static final int SCAN_FOREGROUND_ID = 1;

  private static BluetoothAdapter adapter;
  private static BluetoothLeAdvertiser advertiser;
  private static final BluetoothAdapter.LeScanCallback scanCallback =
      (device, rssi, scanRecord) -> {
    Long id = getID(getUUIDs(scanRecord));
    Integer powerLevel = getTxPowerLevel(scanRecord);
    if (id != null && powerLevel != null)
      delegate.foundDevice(id, powerLevel, rssi);
  };
  ;
  private static final AdvertiseCallback advertiseCallback =
      new AdvertiseCallback();

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    System.err.println("bluetooth advertiser service started");

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Polyfill.startForeground(this, SCAN_FOREGROUND_ID, SCAN_CHANNEL_ID,
                               SCAN_CHANNEL_NAME);
    } else {
      startForeground(SCAN_FOREGROUND_ID, new Notification());
    }

    byte[] overflowData = new byte[17];
    overflowData[0] = 1;
    overflowData[1] = -1 << 7;
    for (int i = 0; i < 8; i++) {
      overflowData[i + 2] = getReversedByte(AppLogic.getBluetoothID(), i);
    }

    AdvertiseSettings settings =
        new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .build();

    AdvertiseData data = new AdvertiseData.Builder()
                             .setIncludeTxPowerLevel(true)
                             .addManufacturerData(0x4C, overflowData)
                             .build();

    advertiser.startAdvertising(settings, data, advertiseCallback);
    adapter.startLeScan(scanCallback);

    return Service.START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    advertiser.stopAdvertising(advertiseCallback);
    adapter.stopLeScan(scanCallback);
  }

  public static boolean isEnabled() {
    adapter = BluetoothAdapter.getDefaultAdapter();
    if (adapter == null) {
      return false;
    }

    if (!adapter.isEnabled())
      return false;

    advertiser = adapter.getBluetoothLeAdvertiser();
    return advertiser != null;
  }

  static ArrayList<UUID> getUUIDs(byte[] bytes) {
    ArrayList<UUID> uuids = new ArrayList<>();
    for (int i = 0, end; i < bytes.length; i = end) {
      int len = bytes[i++];
      if (len <= 0)
        break;
      end = len + i;
      if (len < 3)
        continue;

      if (bytes[i] == 6) { // We're in an incomplete UUID list
        if (len != 17)
          continue; // It's not following the apple behavior

        long first = 0, second = 0;
        for (int j = 0; j < 8; j++)
          first = (first << 8) | bytes[++i];
        for (int j = 0; j < 8; j++)
          second = (second << 8) | bytes[++i];
        uuids.add(
            new UUID(Long.reverseBytes(second), Long.reverseBytes(first)));
        continue;
      }

      if (bytes[i++] != -1)
        continue;
      if (bytes[i++] != 76)
        continue;
      if (bytes[i++] != 0)
        continue;
      if (bytes[i++] != 1)
        continue;

      for (int j = 0; i < end; j++) {
        byte current = bytes[i++];
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
      Integer indexNull = SERVICE_UUIDS_TO_BITS.get(uuid);
      if (indexNull == null) return null;
      int index = indexNull;
      if (index == 0) {
        foundSentinel = true;
        continue;
      }
      id |= 1L << (index - 8);
    }
    if (foundSentinel)
      return id;
    return null;
  }

  static byte getReversedByte(long value, int index) { // TODO make this cleaner
    long byteValue = value >> (index * 8);
    byte y = 0;
    for (int position = 7; position >= 0; position--) {
      y |= ((byteValue & 1) << position);
      byteValue >>= 1;
    }
    return y;
  }

  static Integer getTxPowerLevel(byte[] bytes) {
    for (int i = 0; i < bytes.length; i++) {
      int len = bytes[i];
      if (len == 0)
        break;
      int end = len + i;
      if (len != 2) {
        i = end;
        continue;
      }

      if (bytes[++i] != 10) {
        i = end;
        continue;
      }

      return (int)bytes[++i];
    }
    return null;
  }

  static class AdvertiseCallback
      extends android.bluetooth.le.AdvertiseCallback {
    public AdvertiseCallback() {}
    @Override
    public void onStartSuccess(AdvertiseSettings settingsInEffect) {}
    @Override
    public void onStartFailure(int errorCode) {}
  }
}