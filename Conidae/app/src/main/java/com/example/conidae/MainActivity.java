package com.example.conidae;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnTuning = findViewById(R.id.btnTun);
        btnTuning.setOnClickListener(view -> {
            Intent i = new Intent(this,Tuning.class);
            startActivity(i);
        });
    }
}
