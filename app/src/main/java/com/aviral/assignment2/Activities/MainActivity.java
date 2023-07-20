package com.aviral.assignment2.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.aviral.assignment2.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnUsb.setOnClickListener(view -> startActivity(new Intent(this, USBConnectionActivity.class)));

        binding.btnWifi.setOnClickListener(view -> startActivity(new Intent(this, WifiConnectionActivity.class)));

        binding.btnBluetooth.setOnClickListener(view -> startActivity(new Intent(this, BluetoothConnectionActivity.class)));

    }
}