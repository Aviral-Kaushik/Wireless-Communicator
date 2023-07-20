package com.aviral.assignment2.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.aviral.assignment2.databinding.ActivityUsbconnectionBinding;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class USBConnectionActivity extends AppCompatActivity implements SerialInputOutputManager.Listener {

    private static final String TAG = "USBSensorReader";

    private ActivityUsbconnectionBinding binding;

    private final UsbManager usbManager;
    private UsbDevice usbDevice;
    private UsbSerialPort serialPort;
    private UsbDeviceConnection connection;
    private byte[] response;

    public USBConnectionActivity(Context context) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsbconnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        connectToDevice();

    }

    public void setUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    public void connectToDevice() {
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(usbDevice);
        if (driver == null) {
            Log.d(TAG, "connectToDevice: Connection Error...");
            return;
        }

        setUsbDevice(driver.getDevice());

        connection = usbManager.openDevice(driver.getDevice());
        if (connection == null) {
            Log.d(TAG, "connectToDevice: Connection Error...");
            return;
        }

        serialPort = driver.getPorts().get(0);
        try {
            serialPort.open(connection);
            serialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            serialPort.read(response, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SerialInputOutputManager usbIoManager = new SerialInputOutputManager(serialPort, this);
        usbIoManager.run();
    }

    @Override
    public void onNewData(byte[] data) {

        String receivedData = new String(data, StandardCharsets.UTF_8);
        Log.d(TAG, "Received data: " + receivedData);
        // Continue reading data from the sensor
        byte[] newArray = new byte[response.length + data.length];
        System.arraycopy(response, 0, data, 0, response.length);

        response = newArray;

        binding.usbData.setText(Arrays.toString(response));

        SharedPreferences sharedPreferences = getSharedPreferences("USBData", Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putString("data", receivedData);

        myEdit.apply();

    }

    @Override
    public void onRunError(Exception e) {
        Log.d(TAG, "onRunError: Error: " + e.getMessage());
    }
}