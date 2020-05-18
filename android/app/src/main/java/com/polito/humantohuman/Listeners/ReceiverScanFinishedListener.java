package com.polito.humantohuman.Listeners;

import android.content.Context;

import com.polito.humantohuman.Receivers.ScanReceiver;

/**
 * Listener for when the scan has finished
 */
public interface ReceiverScanFinishedListener { void onScanFinished(ScanReceiver.ScanType type, Context context);
}

