package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button spec = findViewById(R.id.button);
        spec.setOnClickListener(view -> {
            Intent intent = new Intent(getApplication(), SpecActivity.class);
            startActivity(intent);
            finish();

        });

        Button returnacc = findViewById(R.id.button2);
        returnacc.setOnClickListener(view -> {
            Intent intent = new Intent(getApplication(),ReturnAcc.class);
            startActivity(intent);
            finish();
        });

        Button gpsoutput = findViewById(R.id.button5);
        gpsoutput.setOnClickListener(view -> {
            Intent intent = new Intent(getApplication(),ReturnGps.class);
            startActivity(intent);
            finish();
        });

        Button blecom = findViewById(R.id.button6);
        blecom.setOnClickListener(view -> {
            Intent intent = new Intent(getApplication(),BLEcommunicate.class);
            startActivity(intent);
            finish();
        });


    }
}