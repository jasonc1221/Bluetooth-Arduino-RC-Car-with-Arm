package com.example.android.rccararm;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothConnectActivity extends AppCompatActivity {
    //Widgets
    Button enable,paired,scan;
    TextView status;
    ProgressDialog mProgressDlg;
    ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    BluetoothAdapter mBluetoothAdapter;
    BluetoothManager mBluetoothManager;
    BluetoothLeScanner mBluetoothScanner;

    //Stops scanning after 5 seconds
    private android.os.Handler mHandler = new android.os.Handler();
    private static final long SCAN_PERIOD = 5000;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);

        status = (TextView) findViewById(R.id.txt_status);
        enable = (Button) findViewById(R.id.btn_enable);
        paired = (Button) findViewById(R.id.btn_view_paired);
        scan = (Button) findViewById(R.id.btn_scan);
        mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mProgressDlg = new ProgressDialog(this);

        //Checks if the device is bluetooth capable
        if(mBluetoothAdapter == null){
            //Show a message that the device has no bluetooth adapter
            showToast("Bluetooth Adapter Not Available");
            //finish apk
            showUnsupported();
            //finish();
        }

        //Needs to have access coarse location enabled to return results, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        //Enable button to turn on or off bluetooth
        enable.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    showDisabled();
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        });

        //Paired Device button to list paired devices on different activity
        paired.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices == null || pairedDevices.size() == 0) {
                    showToast("No Paired Devices Found");
                } else {
                    ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
                    list.addAll(pairedDevices);
                    Intent intent = new Intent(BluetoothConnectActivity.this, DeviceListActivity.class);
                    intent.putParcelableArrayListExtra("device.list", list);
                    startActivity(intent);
                }
            }
        });

        //Scan button to start scanning
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startScanning();
            }
        });

        //Changes the status of the buttons
        if (mBluetoothAdapter.isEnabled()) {
            showEnabled();
        } else {
            showDisabled();
        }

        //When Scanning appears, can cancel the scanning
        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                stopScanning();
            }
        });
    }

    //Starts the scanning for BLE devices
    public void startScanning() {
        mDeviceList.clear();
        mProgressDlg.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.startScan(mLeScanCallback);
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
                Intent intent = new Intent(BluetoothConnectActivity.this, DeviceListActivity.class);
                intent.putParcelableArrayListExtra("device.list", mDeviceList);
                startActivity(intent);
            }
        }, SCAN_PERIOD);
    }

    //If a BLE device is found, adds the BLE device to mDeviceList
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(!mDeviceList.contains(result.getDevice())) {
//                showToast("Device found: " + result.getDevice().getName());
                mDeviceList.add(result.getDevice());
            }
        }
    };

    public void stopScanning() {
        mProgressDlg.dismiss();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothScanner.stopScan(mLeScanCallback);
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Enabled");
                    showEnabled();
                }
            }
        }
    };

    private void showEnabled() {
        status.setText("Bluetooth is On");
        status.setTextColor(Color.BLUE);

        enable.setText("Disable");
        enable.setEnabled(true);

        paired.setEnabled(true);
        scan.setEnabled(true);
    }

    private void showDisabled() {
        status.setText("Bluetooth is Off");
        status.setTextColor(Color.RED);

        enable.setText("Enable");
        enable.setEnabled(true);

        paired.setEnabled(false);
        scan.setEnabled(false);
    }

    private void showUnsupported() {
        status.setText("Bluetooth is unsupported by this device");

        enable.setText("Enable");
        enable.setEnabled(false);

        paired.setEnabled(false);
        scan.setEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
