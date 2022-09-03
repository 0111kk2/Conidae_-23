package com.example.a0903app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
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
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    LocationManager locationManager;
    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private final float[] rotationFinal = new float[9];

    //変数定義
    double g_lat = 0; //ゴール
    double g_lon = 0;
    double disbet = 0; //その他
    double azimuth = 0;

    //GPSが使用できる様にするやつ
    private void locationStart(){
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 50, this);
    }

    //GPSで目標位置,方位角とか出す
    @Override
    public void onLocationChanged(Location location) {
        //2転換の距離の算出結果を格納する配列
        float[] results = new float[2];
        // 2転換の距離算出
        Location.distanceBetween( location.getLatitude(), location.getLongitude(), g_lat ,g_lon, results);

        //求まった距離と方位角を格納する
        disbet = results[0];
        azimuth = results[1];
        System.out.println(disbet);
        System.out.println(azimuth);
    }






    //main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationStart();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }
    @Override
    protected void onResume(){
        super.onResume();
        //System.out.println(disbet);
        //System.out.println(azimuth);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer != null){
            sensorManager.registerListener(this,accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {//センサがバックグランドになっても動き続けることを阻止するため、pauseの際に停止させる。
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
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
    public void updateOrientationAngles(){//センサ値から値を算出する関数
        //getRotationMatrixで回転行列を取得
        SensorManager.getRotationMatrix(rotationMatrix,null,accelerometerReading,magnetometerReading);
        //getOrientationで取得した回転行列から姿勢角を算出。
        SensorManager.remapCoordinateSystem(rotationMatrix,SensorManager.AXIS_Z,SensorManager.AXIS_MINUS_X,rotationFinal);
        SensorManager.getOrientation(rotationFinal,orientationAngles);


        //テキストビューに値を代入する。他で使う場合は削除
        //System.out.println(orientationAngles[0]*180/Math.PI);//方位角
        //System.out.println(orientationAngles[1]*180/Math.PI);//前後の傾き
        //System.out.println(orientationAngles[2]*180/Math.PI);//左右の傾き
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}









