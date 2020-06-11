package com.polito.humantohuman;

import static com.polito.humantohuman.OverflowAreaUtils.*;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.polito.humantohuman.Activities.ScanActivity;
import com.polito.humantohuman.R;

import java.util.*;

public final class Bluetooth extends Service {
    public interface BluetoothDelegate { void foundDevice(long id, int power, int rssi); }

    public static long id;
    public static BluetoothDelegate delegate = null;
    public static String CHANNEL_ID = "HumanToHuman";
    private static final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private static final BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
    private static final BluetoothAdapter.LeScanCallback scanCallback = (device, rssi, scanRecord) -> {
        Long id = getID(getUUIDs(scanRecord));
        if (id != null)
            delegate.foundDevice(id, getTxPowerLevel(scanRecord), rssi);
    };;
    private static final AdvertiseCallback advertiseCallback = new AdvertiseCallback();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.err.println("bluetooth service started");
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

        getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, ScanActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        adapter.startLeScan(scanCallback);
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

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
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
                continue;
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
            id |= 1L << (index - 8);
        }
        if (foundSentinel)
            return id;
        return null;
    }

    public static byte getReversedByte(long value, int index) { // TODO make this cleaner
        long byteValue = value >> (index * 8);
        byte y = 0;
        for (int position = 7; position >= 0; position--) {
            y |= ((byteValue & 1) << position);
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