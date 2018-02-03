package com.example.telefon.pothole.implementations;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.telefon.pothole.MainActivity;

/**
 * Created by csp4abt on 30.01.2018.
 */

public class BroadcastReceiverImpl extends BroadcastReceiver {

    private final String TAG = BroadcastReceiverImpl.class.getName();

    private MainActivity activity;

    public BroadcastReceiverImpl(MainActivity act) {
        this.activity = act;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // When discovery finds a device
        if (action.equals(activity.getmBluetoothAdapter().ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, activity.getmBluetoothAdapter().ERROR);

            switch(state){
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "onReceive: STATE OFF");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                    break;
            }
        }
    }

}
