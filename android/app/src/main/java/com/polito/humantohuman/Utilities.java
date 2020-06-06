package com.polito.humantohuman;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.polito.humantohuman.ConnsObjects.BtConn;
import com.polito.humantohuman.ConnsObjects.ConnObject;
import com.polito.humantohuman.ConnsObjects.LocationConn;
import com.polito.humantohuman.ConnsObjects.WifiConn;
import com.polito.humantohuman.Database.ConnDatabase;
import com.polito.humantohuman.Serializers.BtConnSerializer;
import com.polito.humantohuman.Serializers.ConnObjectSerializer;
import com.polito.humantohuman.Serializers.LocationConnSerializer;
import com.polito.humantohuman.Serializers.WifiConnSerializer;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

/**
 * Class to some static methods
 */

public class Utilities {
    /**
     * Function to make an md5 hash
     * @param pass
     * @return
     */

    public static String convertPassMd5(String pass) {
        String password = null;
        MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.update(pass.getBytes(), 0, pass.length());
            pass = new BigInteger(1, mdEnc.digest()).toString(16);
            while (pass.length() < 32) {
                pass = "0" + pass;
            }
            password = pass;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return password;
    }

    /** To get the hash of the secureid of the device
     *
     * @param context
     * @return
     */
    public static String getSecureId(Context context){
        String secureId= Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return convertPassMd5(secureId).substring(0,16);
    }

    /**
     * We create a Json with the objects passed
     * @param object
     * @return
     */
    public static String createJson(Object object){
        //Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BtConn.class, new BtConnSerializer())
                .registerTypeAdapter(WifiConn.class, new WifiConnSerializer())
                .registerTypeAdapter(ConnObject.class, new ConnObjectSerializer())
                .registerTypeAdapter(LocationConn.class, new LocationConnSerializer())
                .create();
        return gson.toJson(object);
    }

    /**
     * Method that will get the battery level
     * @param context
     * @return the % of battery
     */
    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;
        return 100;
    }

    /**
     * Method that will get the millis to execute the alarm
     * @param millis
     * @return
     */
    public static Long getMilisForAlarm(long millis){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, (int) millis);
        return cal.getTimeInMillis();

    }

    public static Long getMilisForAlarm() {
        return getMilisForAlarm(0);
    }

    /**
     * Method that will set the alarm
     * @param context The current context from where we are executing the alarm
     * @param pendingIntent The pending Intent with the necessary extras that must be passed to the alarm
     * @param millis How much time should pass to be executed
     */
    public static void setAlarm(Context context, PendingIntent pendingIntent, long millis) {
        long time = getMilisForAlarm(millis);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                            time,pendingIntent);
                } catch (SecurityException exception) {
                    Log.d("Error", "The device has blocked the alarm");
                    System.err.println(exception);
                }

            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                        time,pendingIntent);
            }
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time,pendingIntent);
        }
    }

    public static void setAlarm(Context context, Intent intent, int requestCode, long millis) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,requestCode,intent,0);
        setAlarm(context,pendingIntent,millis);
    }

    public static void cancelAlarm(Context context, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Classes and states that must be initialize before running any service
     * @param context
     */
    public static void initializeApp(Context context) {
        ConnDatabase.getInstance(context);
        DataController.getInstance();
        Constants.Permissions.setPermissions(context);
    }
}
