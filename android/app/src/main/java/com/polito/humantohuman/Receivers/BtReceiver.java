package com.polito.humantohuman.Receivers;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.polito.humantohuman.Listeners.ReceiverScanFinishedListener;
import com.polito.humantohuman.R;
import com.polito.humantohuman.Utilities;

import java.lang.reflect.Method;

import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_NOT_SCANNING;
import static com.polito.humantohuman.Constants.SCAN_STATUS.STATUS_SCANNING;
import static com.polito.humantohuman.Constants.TIME.MAX_TIME_BT_DISCOVERY;

/**
 * This class will handle the bluetooth scan
 */
public class BtReceiver extends BroadcastReceiver {

    /**
     * Constant value, that must be different for every type os ScanReceiver, in order to don't overlap
     * different alarms.
     */
    public final int requestCode;
    /**
     * Enumerator that defines the type of scan used
     */
    protected final ScanType scanType;
    /**
     * The instance of the current bluetooth adapter
     */
    public  BluetoothAdapter bluetoothAdapter;
    /**
     * Instant where last scan has been started
     */
    public long instant;
    /**
     * List of the current listeners that are waiting to the scan to finish
     */
    protected ReceiverScanFinishedListener listener;
    /**
     * Value that defines the status of the scan
     */
    private int status = STATUS_NOT_SCANNING;

    public BtReceiver(ReceiverScanFinishedListener listener) {
        this.scanType = ScanType.BLUETOOTH;
        this.requestCode = 5;
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action =intent.getAction();
        //Case when a device has been found
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //Getting values
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            String macAddress = device.getAddress();
            int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
            //Checking if it is a Phone
            boolean isMobile = true;
            int deviceType = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                deviceType = device.getType();
                isMobile = BluetoothDevice.DEVICE_TYPE_CLASSIC == deviceType;
            }
            return;
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { scanFinished(context);return; }

        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) { return; }

        //When the device is shutting down the BT or BT is already off
        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) { scanFinished(context); return;}

        if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {scanFinished(context); return;}

    }

    protected void onStartScan(Context context) {
        this.bluetoothAdapter = getAdapter(context);

        setBluetooth(true);
        saveOriginalName(context);
        if (!checkPermissions(context)) {  return; }
        if(getStatus() == STATUS_SCANNING){ return; }
        if (bluetoothAdapter.isDiscovering()) {
            //Toast.makeText(context,context.getString(R.string.already_in_discover), Toast.LENGTH_SHORT).show();
            Log.d("Status: ", context.getString(R.string.already_in_discover));
            return;
        }
        enableDiscovery();
        //Setting name of the device
        String idDevice = Utilities.convertPassMd5(Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID)).substring(0, 16);
        bluetoothAdapter.setName(idDevice);
        bluetoothAdapter.startDiscovery();
        Method method;
        try {
            method = bluetoothAdapter.getClass().getMethod("startDiscovery");
            method.invoke(bluetoothAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setStatus(STATUS_SCANNING);
    }

    public void onScanFinished() {
        setStatus(STATUS_NOT_SCANNING);
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        instant = System.currentTimeMillis();
    }

    public boolean checkPermissions(Context context) {
        if(bluetoothAdapter == null ){ return false; }
        //Check that bt is available
        //First we check that we have Localization permissions
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public void clearData() { }

    public void stopScan(Context context) {
        try {
            bluetoothAdapter.cancelDiscovery();
        }catch (Exception e) {
            Log.d("Status" , context.getString(R.string.error_cancel_bt));
        }

        scanFinished(context);
    }

    protected void addReceivers(Context context) {
        try{
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(this,intentFilter);
        } catch (Exception e){ e.printStackTrace(); }
    }


    /**
     * With this method, we can make the device discoverable without asking the user
     */
    public void enableDiscovery() {
        //Alternative to make the device discoverable works for every version!
        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Method method;
            try {
                method = bluetoothAdapter.getClass().getMethod("setScanMode", int.class, int.class);
                method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, MAX_TIME_BT_DISCOVERY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method used to make the device invisible for the other devices
     * @param bluetoothAdapter
     */
    public static void disableDiscovery(BluetoothAdapter bluetoothAdapter) {
        //Alternative to make the device discoverable works for every version!
        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Method method;
            try {
                method = bluetoothAdapter.getClass().getMethod("setScanMode", int.class);
                method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * It will set the name that the device had before using the app
     * @param context
     */
    public static void setOriginalName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        String name = preferences.getString("bluetooth_name","No name");
        getAdapter(context).setName(name);
        disableDiscovery(getAdapter(context));


    }

    /**
     * Called, before the name is changed to make a bakcup of the original name
     * @param context
     */
    public static void saveOriginalName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        if(!preferences.getString("bluetooth_name","").equals("")) {return;}
        String name = getAdapter(context).getName();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("bluetooth_name", name);
        editor.apply();
    }

    /**
     * Get the bluetooth adapter needed for make the scans
     * @param context
     * @return
     */
    public static BluetoothAdapter getAdapter(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            return bluetoothManager.getAdapter();
        } else {
            return BluetoothAdapter.getDefaultAdapter();
        }
    }

    /**
     * A method that should change the state of bluetooth
     * @param enable
     * @return
     */
    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
           return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
           return bluetoothAdapter.disable();
        }
        return true;
    }

    /**
     * In which condition is the adapter at the moment Use class constants to determine the different states
     * @return a constant value that could mean either Scanning or Not scanning
     */
    public int getStatus() { return status;}

    /**
     *
     * @param status Constant that could mean either Scanning or Not scanning
     */
    protected void setStatus(int status){this.status = status;}

    /**
     * What the device should for start scanning
     * @param context
     */
    public void startScan(Context context) {
        addReceivers(context);
        onStartScan(context);
    }

    /**
     * Call the method to handle first the information and call the listener once the scannn has finished
     * @param context
     */
    protected void scanFinished(Context context) {
        if(status == STATUS_SCANNING) {
            onScanFinished();
            notifyListener(context);
            instant = System.currentTimeMillis();
            remoReceivers(context);
        }
    }

    /**
     * Notify the listener that the scan has finished
     * @param context
     */
    protected void notifyListener(Context context){
        this.status = STATUS_NOT_SCANNING;
        listener.onScanFinished(scanType, context);
    }

    protected void remoReceivers(Context context) {
        try{ context.unregisterReceiver(this); } catch (Exception e)  {
            Log.d("Error", "Receiver from " + scanType.toString() + " not removed");
        }
    }

    public enum ScanType {BLUETOOTH, WIFI}
}
