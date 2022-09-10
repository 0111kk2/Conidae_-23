package com.example.a0907;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener{

    LocationManager locationManager;
    SensorManager sensorManager;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private final float[] rotationFinal = new float[9];

    double g_lat ;
    double g_lon ;
    double disbet ;
    double azimuth1 ;
    double azimuth2;
    double deltaarg;


    double s_lat;
    double s_lon;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationStart();

        SeekBar golelat =findViewById(R.id.seekBar2);
        golelat.setMax(360);
        golelat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar golelat, int i, boolean b) {
                g_lat = i;
                TextView g_lat1 = findViewById(R.id.textView);
                g_lat1.setText(String.format("緯度:%d",(int)g_lat));
            }

            @Override
            public void onStartTrackingTouch(SeekBar golelat) {}

            @Override
            public void onStopTrackingTouch(SeekBar golelat) {}
            });

        SeekBar golelon =findViewById(R.id.seekBar);
        golelon.setMax(180);
        golelon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar golelon, int j, boolean b) {
                g_lon = j;
                TextView g_lon1 = findViewById(R.id.textView2);
                g_lon1.setText(String.format("経度:%d",(int)g_lon));
            }

            @Override
            public void onStartTrackingTouch(SeekBar golelon) {}

            @Override
            public void onStopTrackingTouch(SeekBar golelon){}
        });

        Button call =findViewById(R.id.button);
        call.setOnClickListener(view -> {

            float[] results = new float[2];

            g_lat = g_lat - 180;
            g_lon = g_lon - 90;

            // 2転換の距離算出
            Location.distanceBetween( s_lat, s_lon, g_lat ,g_lon, results);

            //求まった距離と方位角を格納する
            disbet = results[0];
            azimuth1 = results[1];
            System.out.println(disbet);
            System.out.println(azimuth1);


            TextView calre = findViewById(R.id.textView3);
            calre.setText(String.format("距離は%.1f m,目標方位角は%.1f °",disbet,azimuth1));

        });
    }


    //GPSが使用できる様にするやつ
    public void locationStart(){
        Log.d("debug","locationStart()");

        // LocationManager インスタンス生成
        locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null && locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER)) {

            Log.d("debug", "location manager Enabled");
        } else {
            // GPSを設定するように促す
            Intent settingsIntent =
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            Log.d("debug", "not gpsEnable, startActivity");
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            Log.d("debug", "checkSelfPermission false");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, this);
    }


    //GPSで目標位置,方位角とか出す
    @Override
    public void onLocationChanged(Location location) {

        s_lat = location.getLatitude();
        s_lon = location.getLongitude();
    }

    @Override
    protected void onPause() {//センサがバックグランドになっても動き続けることを阻止するため、pauseの際に停止させる。
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()== Sensor.TYPE_ACCELEROMETER){
            //arraycopyはセンサの値を代入する方法。引数には、(コピー元,コピー元開始位置,コピー先,コピー先開始,コピーする要素数)の形。
            System.arraycopy(sensorEvent.values,0,accelerometerReading,
                    0,accelerometerReading.length);
        }
        else if(sensorEvent.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(sensorEvent.values,0,magnetometerReading,
                    0,magnetometerReading.length);
        }
        updateOrientationAngles();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void updateOrientationAngles(){//センサ値から値を算出する関数
        //getRotationMatrixで回転行列を取得
        SensorManager.getRotationMatrix(rotationMatrix,null,accelerometerReading,magnetometerReading);
        //getOrientationで取得した回転行列から姿勢角を算出。
        SensorManager.remapCoordinateSystem(rotationMatrix,SensorManager.AXIS_Z,SensorManager.AXIS_MINUS_X,rotationFinal);
        SensorManager.getOrientation(rotationFinal,orientationAngles);

        azimuth2 = orientationAngles[0];
        deltaarg = azimuth2-azimuth1;

        System.out.println(orientationAngles[0]*180/Math.PI);//方位角
        System.out.println(orientationAngles[1]*180/Math.PI);//前後の傾き
        System.out.println(orientationAngles[2]*180/Math.PI);//左右の傾き
    }






}