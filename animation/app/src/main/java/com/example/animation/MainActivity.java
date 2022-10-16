package com.example.animation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button spec = findViewById(R.id.runmode);
        spec.setOnClickListener(view -> {
            Intent intent = new Intent(getApplication(), ConidaeFace.class);
            startActivity(intent);
        });

    }


}