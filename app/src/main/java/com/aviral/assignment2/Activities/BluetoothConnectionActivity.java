package com.aviral.assignment2.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.aviral.assignment2.databinding.ActivityBluetoothConnectionBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnectionActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothConnectionActivity";
    private ActivityBluetoothConnectionBinding binding;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private final static int REQUEST_ENABLE_BT = 1;
    private final static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private StringBuilder receivedDataBuilder;

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                String receivedData = (String) msg.obj;
                receivedDataBuilder.append(receivedData);
                binding.bluetoothData.setText(receivedDataBuilder.toString());

                addDataToSharedPreferences(receivedDataBuilder);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBluetoothConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receivedDataBuilder = new StringBuilder();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Snackbar.make(
                    binding.layoutBluetooth,
                    "Bluetooth is not supported on this device",
                    Snackbar.LENGTH_SHORT
            ).show();

            Log.d(TAG, "onCreate: Bluetooth is not supported on this device");

        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(
                            binding.layoutBluetooth,
                            "Please allow bluetooth permission",
                            Snackbar.LENGTH_SHORT
                    ).show();
                    return;
                }
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
            } else {
                connectToSensor();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                connectToSensor();
            } else {
                Snackbar.make(
                        binding.layoutBluetooth,
                        "Bluetooth was not enabled",
                        Snackbar.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void connectToSensor() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(
                    binding.layoutBluetooth,
                    "Please allow bluetooth permission",
                    Snackbar.LENGTH_SHORT
            ).show();
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice sensorDevice = null;

        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("SensorDeviceName")) {
                sensorDevice = device;
                break;
            }
        }

        if (sensorDevice == null) {
            Toast.makeText(getApplicationContext(), "Sensor device not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            bluetoothSocket = sensorDevice.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            Snackbar.make(
                    binding.layoutBluetooth,
                    "Connected to sensor device",
                    Snackbar.LENGTH_SHORT
            ).show();

            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();

            startReceivingData();
        } catch (IOException e) {
            Log.d(TAG, "disconnectFromSensor: Exception occurred: " + e.getMessage());

            Snackbar.make(
                    binding.layoutBluetooth,
                    "Error connecting to sensor device",
                    Snackbar.LENGTH_SHORT
            ).show();

            e.printStackTrace();
        }
    }

    private void disconnectFromSensor() {
        if (bluetoothSocket != null) {
            try {

                inputStream.close();
                outputStream.close();
                bluetoothSocket.close();

                Snackbar.make(
                        binding.layoutBluetooth,
                        "Disconnected from sensor device",
                        Snackbar.LENGTH_SHORT
                ).show();
            } catch (IOException e) {
                Log.d(TAG, "disconnectFromSensor: Exception occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void startReceivingData() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String receivedData = new String(buffer, 0, bytes);
                    handler.obtainMessage(0, receivedData).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    private void addDataToSharedPreferences(StringBuilder receivedDataBuilder) {

        SharedPreferences sharedPreferences = getSharedPreferences("BluetoothData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("data", String.valueOf(receivedDataBuilder));

        editor.apply();

    }
}