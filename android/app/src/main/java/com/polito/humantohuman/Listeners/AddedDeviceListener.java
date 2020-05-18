package com.polito.humantohuman.Listeners;

/**
 * Listener, to change the state of an object, after the device been registered in the server
 */
public interface AddedDeviceListener {
     void onAddedDeviceListener(boolean correct);
}
