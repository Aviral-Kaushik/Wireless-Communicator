package com.aviral.assignment2.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aviral.assignment2.databinding.ActivityWifiConnectionBinding;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class WifiConnectionActivity extends AppCompatActivity {

    private ActivityWifiConnectionBinding binding;

    private static final String IP_ADDRESS = "192.168.0.100";
    private static final int PORT = 1234;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWifiConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("WifiData", Context.MODE_PRIVATE);

        new ReadDataFromSensor().execute();
    }


    private class ReadDataFromSensor extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Socket socket = new Socket(IP_ADDRESS, PORT);

                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                final String receivedData = new String(buffer, 0, bytesRead);

                runOnUiThread(() -> {
                    binding.wifiData.setText(receivedData);
                    saveDataToSharedPreferences(receivedData);
                });

                inputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void saveDataToSharedPreferences(String receivedData) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("data", receivedData);
        editor.apply();
    }

}