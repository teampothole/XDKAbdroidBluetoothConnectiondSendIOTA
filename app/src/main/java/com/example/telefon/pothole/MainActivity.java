package com.example.telefon.pothole;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.MainThread;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.telefon.pothole.implementations.BluetoothGattCallbackImpl;
import com.example.telefon.pothole.implementations.BroadcastReceiverImpl;
import com.example.telefon.pothole.implementations.ScanCallbackImpl;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final String XDK_BLUETOOTH_ID = "FC:D6:BD:10:07:C7"; //Meine Nummer! Wichtig!!! (XDK Bluttooth Nummer)
    private TextView connectionTextField, value1TextField, value2TextField, value3TextField;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 6000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    int wallet = 200;
    private TextView wallettext;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    private Button btnONOFF;
    private String urlAdress = "http://localhost:3000/notify";
    private Location loca;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    Date time1 =  new Date();

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiverImpl(this);

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Log.i("test", "on resume else block");
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
    }

    public int MakeRandomNumber(int min, int max){
        Random random = new Random();

        int randomnumer = random.nextInt((max - min)+1)+min;

        return randomnumer;
    }

    public void sendToTangle(){
        String[] data = new String[3];
        data[0] = "Tag";
        data[1] = String.valueOf(MakeRandomNumber(0,10));
        data[2] = String.valueOf(MakeRandomNumber(0,10));

        sendPost(data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnONOFF = (Button) findViewById(R.id.btnOnOff);

        wallettext = (TextView) findViewById(R.id.wallet);
        wallettext.setText("Wallet " + wallet);
        wallettext.setTextColor(Color.BLACK);

        list = (ListView) findViewById(R.id.infoList);
        list.setBackgroundColor(Color.WHITE);
        list.setFastScrollEnabled(true);
        list.setFastScrollAlwaysVisible(true);
        arrayList = new ArrayList<String>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);

        // Here, you set the data in your ListView
        list.setAdapter(adapter);
        //list.setClickable(true);

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                String selectedFromList =(String) (list.getItemAtPosition(myItemInt));
                //list.removeViewInLayout(myView);
                arrayList.remove(myItemInt);
                adapter.notifyDataSetChanged();
                wallettext.setText( "Pos: " + list.getAdapter().getItemId(myItemInt) );
                wallettext.setVisibility(View.VISIBLE);




            }
        });

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        Log.e("TEST", "" + permissionCheck);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH}, 1);
        }
        //Check if Bluetooth LE is supported
        mHandler = new Handler();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        checkBTStateToColor();
    }

    public void sendPost(final String[] message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlAdress);

                    // some Options
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();

                    //add here some values
                    jsonParam.put("timestamp", new Timestamp(System.currentTimeMillis()));
                    jsonParam.put("uname", message[0]);
                    jsonParam.put("message", message[1]);
                    jsonParam.put("latitude", message[2]);

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void getLocation() {

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                loca = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
            // Here, thisActivity is the current activity

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, locationListener);
        }


    }


    public void upDateList(){
        adapter.notifyDataSetChanged();
    }

    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            if(mGatt != null) {
                mGatt.connect();
            }
            // next thing you have to do is check if your adapter has changed
            //text.setText("Wallet "+ wallet);
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            mBluetoothAdapter.enable();
        }
        if(mBluetoothAdapter.isEnabled()){
            // next thing you have to do is check if your adapter has changed
            Log.d(TAG, "enableDisableBT: disabling BT.");

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            mBluetoothAdapter.disable();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }


    private ScanCallback mScanCallback = new ScanCallbackImpl(this);

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        Log.i("gatt0", device.toString());
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            Log.i("gatt2", mGatt.toString());
            //connectionTextField.setText("Connected");
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallbackImpl(this);
    int time = 0;
    public void addItemToList(String characteristic){
        //if(Double.parseDouble(characteristic) > 20 || Double.parseDouble(characteristic) < -20){
            //Log.e("LogDouble", ":" + Double.parseDouble(characteristic));
        //}

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Collections.reverse(arrayList);



                            time --;
                            try {
                                if (Double.parseDouble(characteristic) > 20 || Double.parseDouble(characteristic) < -20) {


                                    if(time < 0) {

                                        Log.i("First Send", characteristic);
                                        arrayList.add(0, "PotHole Detected");
                                        adapter.notifyDataSetChanged();
                                        sendToTangle();
                                        time = 45;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("not works", e.toString());
                            }
                    //sendToTangle();

                checkBTStateToColor();
//stuff that updates ui

            }
        });

        //arrayList.add(characteristic);
        //adapter.notifyDataSetChanged();
    }

    private void checkBTStateToColor() {
        if(mBluetoothAdapter.isEnabled()){
            btnONOFF.setText("Disable BT");
            btnONOFF.setBackgroundColor(Color.LTGRAY);
            btnONOFF.setTextColor(Color.RED);
        }
        if(!mBluetoothAdapter.isEnabled()){
            btnONOFF.setText("Enable BT");
            btnONOFF.setBackgroundColor(Color.BLUE);
            btnONOFF.setTextColor(Color.WHITE);
        }
    }

    public String getXdkBluetoothId() {
        return XDK_BLUETOOTH_ID;
    }
    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }
}
