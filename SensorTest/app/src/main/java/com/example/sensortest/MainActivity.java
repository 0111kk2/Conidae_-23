package com.example.sensortest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;
    private TextView txtLat,txtLon,txtLeft,txtRight,txtBlue,txtAccuracy,txtSpeak;
    private Button btnStart,btnStop;
    private InductionConidae conidae = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        txtLat = (TextView) findViewById(R.id.txtLatitude);
        txtLon = (TextView) findViewById(R.id.txtLongitude);
        txtLeft = (TextView) findViewById(R.id.txtLeftOutput);
        txtRight = (TextView) findViewById(R.id.txtRightOutput);
        txtBlue = (TextView) findViewById(R.id.txtBluetoothState);
        txtAccuracy = (TextView) findViewById(R.id.txtSensorAccuracy);
        txtSpeak = (TextView) findViewById(R.id.txtConidaeSpeak);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);

        InductionConidae.setGoal(35.77193599712731,139.8623666081448);
        btnStart.setOnClickListener(view -> {
            if(conidae==null){
                conidae = new InductionConidae();
            }
        });
        btnStop.setOnClickListener(view -> {
            if(conidae!=null){
                conidae.quit();
                conidae = null;
            }
        });
    }
    //コンテクストのゲッター
    public static MainActivity getInstance(){
        return instance;
    }
    //セッター類
    public void setTxtLat(String text) {
        txtLat.setText(text);
    }
    public void setTxtLon(String text){
        txtLon.setText(text);
    }
    public void setTxtLeft(String text){
        txtLeft.setText(text);
    }
    public void setTxtRight(String text){
        txtRight.setText(text);
    }
    public void setTxtBlue(String text){
        txtBlue.setText(text);
    }
    public void setTxtAccuracy(String text){
        txtAccuracy.setText(text);
    }
    public void setTxtSpeak(String text){
        txtSpeak.setText(text);
    }
}