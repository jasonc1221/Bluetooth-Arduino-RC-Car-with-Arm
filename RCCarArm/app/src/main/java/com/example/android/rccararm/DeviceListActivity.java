package com.example.android.rccararm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class DeviceListActivity extends AppCompatActivity {

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private ListView mListView;
    private DeviceListAdapter mAdapter;
    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapater;
    BluetoothGatt mBluetoothGatt;
    BluetoothDevice mBluetoothDevice;
    private ArrayList<BluetoothDevice> mDeviceList;
    public static final String EXTRA_DEVICE = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        //Sets up Bluetooth Manager and Adapter
        mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapater = mBluetoothManager.getAdapter();

        //Gets the list of devices found from BluetoothConnectActivity
        mDeviceList	= getIntent().getExtras().getParcelableArrayList("device.list");
        mListView = (ListView) findViewById(R.id.lv_paired);
        mAdapter = new DeviceListAdapter(this);
        mAdapter.setData(mDeviceList);
        mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position) {
                BluetoothDevice device = mDeviceList.get(position);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    unpairDevice(device);
                } else {
                    pairDevice(device);
                }
            }
        });
        mListView.setAdapter(mAdapter);
    }

    // Fast way to call Toast
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //Connects phone to BluetoothLE
    private void pairDevice(BluetoothDevice device) {
        mBluetoothDevice = device;
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    }

    private void unpairDevice(BluetoothDevice device) {
        mBluetoothGatt.disconnect();
    }

    //Checks the state of the BluetoothGatt connection
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // This will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case STATE_DISCONNECTED:
                    DeviceListActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            showToast("Device Disconnected");
                        }
                    });
                    break;
                case STATE_CONNECTING:
                    showToast("Pairing...");
                case STATE_CONNECTED:
                    DeviceListActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            showToast("Device Paired");
                            Intent intent = new Intent(DeviceListActivity.this, ControlActivity.class);
                            intent.putExtra(EXTRA_DEVICE, mBluetoothDevice);
                            startActivity(intent);
                        }
                    });
                    break;
                default:
                    DeviceListActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            showToast("We encountered an unknown state, uh oh");
                        }
                    });
                    break;
            }
        }
    };
}
