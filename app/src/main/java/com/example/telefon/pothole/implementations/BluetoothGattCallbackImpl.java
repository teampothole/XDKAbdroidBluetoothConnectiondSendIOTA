package com.example.telefon.pothole.implementations;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.SystemClock;
import android.util.Log;

import com.example.telefon.pothole.MainActivity;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by csp4abt on 30.01.2018.
 */

public class BluetoothGattCallbackImpl extends BluetoothGattCallback {

    private final String TAG = BluetoothGattCallbackImpl.class.getName();

    private MainActivity activity;

    public BluetoothGattCallbackImpl(MainActivity act) {
        this.activity = act;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.i("onConnectionStateChange", "Status: " + status);
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                Log.i("gattCallback", "STATE_CONNECTED");
                //connectionTextField.setText("Connected");
                gatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                Log.e("gattCallback", "STATE_DISCONNECTED");
                //connectionTextField.setText("Disconnected");
                break;
            default:
                Log.e("gattCallback", "STATE_OTHER");
        }

    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        String TAG = "testen";

        BluetoothGattService serviceBasic = gatt.getService(UUID.fromString("00001801-0000-1000-8000-00805f9b34fb"));
        if (serviceBasic == null) {
            Log.e(TAG, "service not found!");
        }
        BluetoothGattCharacteristic characteristicBasic = serviceBasic.getCharacteristic(UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb"));
        if (characteristicBasic == null) {
            Log.e(TAG, "char not found!");
        }

        BluetoothGattDescriptor descriptor = characteristicBasic.getDescriptor(UUID.fromString("000002902-0000-1000-8000-00805f9b34fb"));

        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);



        BluetoothGattService service = gatt.getService(UUID.fromString("b9e875c0-1cfa-11e6-b797-0002a5d5c51b"));
        if (service == null) {
            Log.e(TAG, "service not found!");
        }

        BluetoothGattCharacteristic characteristicNotify = service.getCharacteristic(UUID.fromString("1ed9e2c0-266f-11e6-850b-0002a5d5c51b"));
        if (characteristicNotify == null) {
            Log.e(TAG, "char not found!");
        }
        gatt.setCharacteristicNotification(characteristicNotify, true);
    }


    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.e("descriptor write","test" + descriptor.getValue().toString());

        BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString("b9e875c0-1cfa-11e6-b797-0002a5d5c51b"))
                .getCharacteristic(UUID.fromString("0c68d100-266f-11e6-b388-0002a5d5c51b"));
        characteristic.setValue("start");
        gatt.writeCharacteristic(characteristic);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.i("teswtt", "" + characteristic);
        //gatt.disconnect();
        //gatt.close();
    }
    Timestamp timefirst = new Timestamp(System.currentTimeMillis());
    Timestamp timesecond = new Timestamp(System.currentTimeMillis()+10000);

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        SystemClock.sleep(10);
        //Log.e("funkt", characteristic.getStringValue(0));

       // if(timefirst.getTime()<timesecond.getTime()) {
            activity.addItemToList(characteristic.getStringValue(0));
            //activity.sendToTangle();
       // }

        //adapter.notifyDataSetChanged();
        byte[] data = characteristic.getValue();
        //Log.e("data", ":" + data.length);
    }

}
