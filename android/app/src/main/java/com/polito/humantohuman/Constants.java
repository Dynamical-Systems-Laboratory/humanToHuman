package com.polito.humantohuman;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Constants {

    public static final int MAX_ITEMS_DATABASE = 60;
    /**
     * Current version of the application
     */
    private static int VERSION = Build.VERSION.SDK_INT;
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH = 1;

    public static class TIME {

        /**
         * MAX_TIME_BT_DISCOVERY = How much time should be the device in discoverable mode
         */
        public static final int MAX_TIME_BT_DISCOVERY = 3600;
        /**
         * NOTIFY_INTERVAL = The interval of time where the service for start the scanning service should be called
         */
        public static final long NOTIFY_INTERVAL =  1000 * (VERSION >= Build.VERSION_CODES.N ? 20 : 30) ;
        /**
         * NOTIFY_INTERNET_INTERVAL = The interval of time where the alarm for the  uploadservice should be called
         */
        public static final long NOTIFY_INTERNET_INTERVAL = 1000 * 30;

        /**
         * MAX_TIME_BT_SCAN = The maximum time of a bluetooth scan
         */
        //public static final long MAX_TIME_BT_SCAN = VERSION >= Build.VERSION_CODES.N ? MAX_TIME_BLE_SCAN : 40000 ;
        public static final long MAX_TIME_BT_SCAN = 40000 ;

        /**
         * MAX_TIME_WIFI_SCAN = The maximum time of a wifi scan
         */
        public static final long MAX_TIME_WIFI_SCAN = 40000;
        /**
         * MAX_TIME_UPLOAD = Value that will determine if the application should upload local information or not
         */
        public static final long MAX_TIME_UPLOAD = 100 * 1000;
        /**
         * SEND_SAVE_DATA_THREAD_TIME = The time that the handler should wait before uploading another object from the local database
         */
        public static final long SEND_SAVE_DATA_THREAD_TIME = 2000;
        /**
         * TIME_OUT = How much time should pass before consider that we haven't received a response from the server
         */
        public static final int TIME_OUT = 3000;
        /**
         * RESPONSE_TIME_OUT = How much time should pass before consider that we haven't received a response from the server
         */
        public static final int RESPONSE_TIME_OUT = 5000;
        /**
         * How much time it should pass before restarting the service
         */
        public static final long RESTART_TIME_SERVICE = 1000 * 60 ;

        /**
         * MAX_TIME_LOCALIZATION = This value will determine how much time should pass before updating the localization
         */

        public static final long MAX_TIME_LOCALIZATION = 1000 * 60 * 60;
    }

    /**
     * Class that will store the possible actions that could be execute on the service
     */
    public static class SERVICE_ACTION {
        /**
         * If we tell the service to be restart
         */
        public static final String RESTART_FOREGROUND_INTENT = "Restart";
        /**
         * If we tell the service to Start
         */
        public static final String START_FOREGROUND_INTENT = "Start";
        /**
         * If we tell the service to Stop
         */
        public static final String STOP_FOREGROUND_INTENT = "Stop";
    }


    public static final int MAX_UPLOADS = 30;

    public static final int MAX_BATTERY = 15;
    /**
     * Class that will store the url and the different points of the server
     */
    public static class SERVER_ENDPOINT {

        public static String SERVER_URL = "http://btserver-lab10.polito.it";

        public static String CONN_INFO_ENDPOINT = SERVER_URL +"/conn_info";
        public static String DEVICES_ENDPOINT = SERVER_URL +"/devices";
        public static String ID_DEVICE_ENDPOINT = SERVER_URL +"/id_device";
    }


    //public static String CONN_INFO_ENDPOINT = "http://192.168.1.104:5000/conn_info";
    //public static String CONN_INFO_ENDPOINT = "http://192.168.1.104/conn_info";

    public static int JOB_ID = 1000;
    /**
     * Class that will determine the status of an Scan
     */
    public static class SCAN_STATUS {

        public static final int STATUS_NOT_SCANNING = 1;
        public static final int STATUS_SCANNING = 0;
    }
    /**
     * Class that will store the values of the screen status
     */
    public static class SCREEN_STATUS  {

        public static final int ON = 1;
        public static final int OFF = 0;
    }
    /**
     * Class that stores the values of the server status
     */
    public static class STATUS {

        public static int NOT_RUNNING = 0;
        public static int RUNNING = 1;
    }

    /*public static void needToStart(Activity activity) {


    }*/

    /**
     * It will store the current state of the permissions
     */
    public static class Permissions{
        /**
         * List of the permissions that the app could request
         */
        public final static List<Permission> permissions = new ArrayList<>();
        private static int pCheck = PackageManager.PERMISSION_GRANTED;

        /**
         * Define wich permissions we need to request
         */
        static {
           permissions.add(new Permission(ACCESS_COARSE_LOCATION,false));
           permissions.add(new Permission(ACCESS_FINE_LOCATION,false));
        }

        /**
         * Check if a permission ahs been accepted or no
         * @param context
         * @param pString Manifest string that will check if it hsa been accepted or not
         * @return
         */
        public static boolean checkPermission(Context context, String pString) {
            setPermissions(context);
            for(Permission p : permissions) {
                if(p.permission.equals(pString)) {
                    return p.accepted;
                }
            }
            return true;
        }

        /**
         * Check if all the permissions has been accepted or not
         * @param context
         * @return
         */
        public static boolean checkPermissions(Context context) {
            setPermissions(context);
            for(Permission p : permissions) {
                if(!p.accepted) { return false; }
            }
            return true;
        }

        /**
         * Request the user to accept all the permissions
         * @param activity
         */
        public static void requestPermissions(Activity activity) {
            ArrayList<String> sPermissions = new ArrayList<>();
            for (Permission p : permissions) {
                if (!p.accepted) {
                    sPermissions.add(p.permission);
                }
            }
            String[] arrayP = sPermissions.toArray(new String[sPermissions.size()]);
            ActivityCompat.requestPermissions(activity, arrayP, REQUEST_ACCESS_COARSE_LOCATION);
        }

        /**
         * Set the current state of the permissions Not accepted / Accepted
         * @param context
         */
        public static void setPermissions(Context context) {
            for(Permission p : permissions) { setPermission(context,p); }
        }

        /**
         * Set the current state of a permission Not accepted / Accepted
         * @param context
         * @param p
         */
        public static void setPermission(Context context,Permission p) {
            p.setAccepted(ContextCompat.checkSelfPermission(context, p.getPermission()) == pCheck);
        }
        /**
         * Class that will store the information about a permission
         */
        public static class Permission{
            private String permission;
            private boolean accepted;
            public Permission (String permission,boolean accepted) {
                this.permission = permission;
                this.accepted = accepted;
            }

            public String getPermission() { return permission; }
            public void setPermission(String permission) { this.permission = permission; }
            public boolean isAccepted() { return accepted; }
            public void setAccepted(boolean accepted) { this.accepted = accepted; }
        }

    }

}
