package com.example.telefon.pothole.implementations;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.example.telefon.pothole.MainActivity;

import java.util.List;

/**
 * Created by csp4abt on 30.01.2018.
 */

public class ScanCallbackImpl extends ScanCallback {

    private final String TAG = ScanCallbackImpl.class.getName();

    private MainActivity activity;

    public ScanCallbackImpl(MainActivity act) {
        this.activity = act;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        Log.i("callbackType", String.valueOf(callbackType));
        Log.i("result", result.toString());
        BluetoothDevice btDevice = result.getDevice();
        if(btDevice.getAddress().equals(activity.getXdkBluetoothId())){
            activity.connectToDevice(btDevice);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult sr : results) {
            Log.i("ScanResult - Results", sr.toString());
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }

}
