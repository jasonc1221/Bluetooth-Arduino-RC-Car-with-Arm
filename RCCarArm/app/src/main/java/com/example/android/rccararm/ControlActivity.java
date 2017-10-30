package com.example.android.rccararm;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {

    Button btnC, btnCC, btnRetract, btnExtend, btnUp, btnDown, btnRelease, btnGrab, btnDisconnect,
            btnForward, btnReverse, btnLeft, btnRight, btnStop;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    BluetoothDevice mBluetoothDevice;
    BluetoothGatt mBluetoothGatt;
    BluetoothGattCharacteristic characteristicRX;
    BluetoothGattCharacteristic characteristicTX;
    static final UUID HM_RX_TX = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //Gets the BluetoothDevice from DeviceListActivity and connects to BluetoothGatt
        mBluetoothDevice = getIntent().getExtras().getParcelable(DeviceListActivity.EXTRA_DEVICE);
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);

        btnC = (Button) findViewById(R.id.but_C);
        btnCC = (Button) findViewById(R.id.but_CC);
        btnRetract = (Button) findViewById(R.id.but_retract);
        btnExtend = (Button) findViewById(R.id.but_extend);
        btnDown = (Button) findViewById(R.id.but_down);
        btnUp = (Button) findViewById(R.id.but_up);
        btnRelease = (Button) findViewById(R.id.but_release);
        btnGrab = (Button) findViewById(R.id.but_grab);
        btnDisconnect = (Button) findViewById(R.id.but_disconnect);
        btnForward = (Button) findViewById(R.id.but_forward);
        btnReverse = (Button) findViewById(R.id.but_reverse);
        btnLeft = (Button) findViewById(R.id.but_left);
        btnRight = (Button) findViewById(R.id.but_right);
        btnStop = (Button) findViewById(R.id.but_stop);

        btnC.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("C\n");
            }
        }));
        btnCC.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("c\n");
            }
        }));
        btnRetract.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("R\n");
            }
        }));
        btnExtend.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("E\n");
            }
        }));
        btnDown.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("D\n");
            }
        }));
        btnUp.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("U\n");
            }
        }));
        btnRelease.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("r\n");
            }
        }));
        btnGrab.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("G\n");
            }
        }));
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendSignal("0\n");
            }
        });
        btnForward.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("w\n");
            }
        }));
        btnReverse.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("s\n");
            }
        }));
        btnLeft.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("a\n");
            }
        }));
        btnRight.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSignal("d\n");
            }
        }));
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendSignal("1\n");
            }
        });
    }

    //Writes Characteristic to Arduino Bluetooth module
    void sendSignal(String str) {
        byte[] tx = str.getBytes();
        characteristicTX.setValue(tx);
        mBluetoothGatt.writeCharacteristic(characteristicTX);
        setCharacteristicNotification(characteristicRX, true);
    }

    // Fast way to call Toast
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //Checks the state of the BluetoothGatt connection
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case STATE_DISCONNECTED:
                    ControlActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            showToast("Device Disconnected");
                            Intent intent = new Intent(ControlActivity.this, DeviceListActivity.class);
                            startActivity(intent);
                        }
                    });
                    break;
                case STATE_CONNECTING:
                    showToast("Pairing...");
                case STATE_CONNECTED:
                    ControlActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            showToast("Device Paired");
                            // Attempts to discover services after successful connection.
                            showToast("Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                        }
                    });

                    // discover services and characteristics for this device
                    mBluetoothGatt.discoverServices();

                    break;
                default:
                    ControlActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            showToast("We encountered an unknown state, uh oh");
                        }
                    });
                    break;
            }
        }

        //Called when BluetoothGatt.discoveredServices() is called
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            ControlActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    showToast("Device services have been discovered");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        for (BluetoothGattService gattService : mBluetoothGatt.getServices()) {
                            //get characteristic when UUID matches RX/TX UUID
                            characteristicTX = gattService.getCharacteristic(HM_RX_TX);
                            characteristicRX = gattService.getCharacteristic(HM_RX_TX);
                        }
                    } else {
                        showToast("onServicesDiscovered received: " + status);
                    }
                }
            });
        }
    };

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        this.mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (HM_RX_TX.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            this.mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
