package com.polito.humantohuman.Listeners;

import android.content.Context;

import com.polito.humantohuman.Receivers.BtReceiver;

/**
 * Listener for when the scan has finished
 */
public interface ReceiverScanFinishedListener { void onScanFinished(BtReceiver.ScanType type, Context context);
}

