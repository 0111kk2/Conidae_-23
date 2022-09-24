package com.example.conidae3rd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance = null;
    //↓山岸宅
    //35.71991425470972, 139.9104316936887
    //理科大図書館前(カツシカ)
    //35.77193599712731, 139.8623666081448
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Contextはthisによって渡されるらしい。
        instance = this;
        setContentView(R.layout.activity_main);
        InductionConidae.setGoal(35.77193599712731,139.8623666081448);
        InductionConidae conidae = new InductionConidae();
        Button btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(view -> conidae.quit());
    }
    //センサの登録やGPSの初期化に必要なオブジェクトであるContextを取得するためのゲッター
    public static MainActivity getInstance() {
        return instance;
    }
}