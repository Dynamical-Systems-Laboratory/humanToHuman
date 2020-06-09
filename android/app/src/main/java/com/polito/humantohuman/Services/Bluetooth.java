package com.polito.humantohuman.Services;

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

public final class Bluetooth {
    public interface BluetoothDelegate { void foundDevice(long id, int power, int rssi); }

    private static BluetoothLeAdvertiser advertiser;
    private static BluetoothAdapter adapter;
    private static BluetoothAdapter.LeScanCallback scanCallback;
    private static AdvertiseCallback advertiseCallback;

    private Bluetooth() {}

    public static void start(BluetoothDelegate delegate) {
        if (adapter == null)adapter = BluetoothAdapter.getDefaultAdapter();
        if (advertiser == null) advertiser = adapter.getBluetoothLeAdvertiser();
        if (scanCallback == null) scanCallback = (device, rssi, scanRecord) -> {
            Long id = getID(getUUIDs(scanRecord));
            if (id != null)
                delegate.foundDevice(id, getTxPowerLevel(scanRecord), rssi);
        };
        if (advertiseCallback == null) advertiseCallback = new AdvertiseCallback();

        adapter.startLeScan(scanCallback);
        long id = 120_000_000;
        byte[] overflowData = new byte[17];
        overflowData[0] = 1;
        overflowData[1] = -1 << 7;
        for (int i = 0; i < 8; i++) {
            overflowData[i + 2] = getReversedByte(id, i);
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
    }

    public static void stop() {
        adapter.stopLeScan(scanCallback);
        advertiser.stopAdvertising(advertiseCallback);
    }

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
        byte byteValue = (byte)((value >> (index * 8)) & ~-256);
        byte y = 0;
        for (int position = 7; position > 0; position--) {
            y += ((byteValue & 1) << position);
            byteValue >>= 1;
        }
        return y;
    }

    public static Integer getTxPowerLevel(byte[] bytes) {
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



    static class AdvertiseCallback extends android.bluetooth.le.AdvertiseCallback {
        public AdvertiseCallback() {}
        @Override public void onStartSuccess(AdvertiseSettings settingsInEffect) {}
        @Override public void onStartFailure(int errorCode) {}
    }
}