package com.example.conidae3rd;

import android.Manifest;
import android.app.Activity;
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
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

class Shell implements SensorEventListener , LocationListener {
    //姿勢角関連
    static SensorManager sensorManager = null;//これはmain関数内でインスタンス名.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE)として初期化。
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] rotationFinal = new float[9];
    private final float[] orientationAngles = new float[3];
    //GPS関連
    private LocationManager locationManager;
    private double nowLat;
    private double nowLon;
    private Context mContext = MainActivity.getInstance();
    //Bluetooth関連
    private BluetoothCommunication blue;
    //コンストラクタ
    Shell(){
        System.out.println("ブルルルル...(エンジン音)");
        blue = new BluetoothCommunication("ESP32test");
        blue.StartBluetoothConnection("ESP32test");
        System.out.println("位置情報シュトクカイシ");
        locationStart();
        System.out.println("センサ値シュトクカイシ");
        onResume();
    }
    //姿勢角関連
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public void onResume(){
        sensorManager= (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer != null){
            sensorManager.registerListener(this,accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magneticField != null){
            sensorManager.registerListener(this,magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL,SensorManager.SENSOR_DELAY_UI);
        }
    }
    public void onPause(){
        sensorManager.unregisterListener(this);
    }

    public void updateOrientationAngles(){//センサ値から値を算出する関数
        //getRotationMatrixで回転行列を取得
        SensorManager.getRotationMatrix(rotationMatrix,null,accelerometerReading,magnetometerReading);
        //getOrientationで取得した回転行列から姿勢角を算出。
        SensorManager.remapCoordinateSystem(rotationMatrix,SensorManager.AXIS_Z,SensorManager.AXIS_MINUS_X,rotationFinal);
        SensorManager.getOrientation(rotationFinal,orientationAngles);
    }
    //============================================location関連
    @Override
    public void onLocationChanged(@NonNull Location location) {
        nowLat = location.getLatitude();
        nowLon = location.getLongitude();
    }
    public void axel(int left,int right){
        System.out.println("シュツリョクシマス"+left+":"+right);
        blue.sendData((90+left)+","+(90-right)+";");
    }
    //現在位置更新の関数
    public void locationStart(){
        Log.d("debug","locationStart()");

        // LocationManager インスタンス生成
        System.out.println(mContext);
        locationManager =
                (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null && locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER)) {

            Log.d("debug", "location manager Enabled");
        } else {
            // GPSを設定するように促す
            Intent settingsIntent =
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(settingsIntent);
            Log.d("debug", "not gpsEnable, startActivity");
        }

        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            Log.d("debug", "checkSelfPermission false");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, this);
    }
    //+getter類
    public float[] getOrientation(){
        return orientationAngles;
    }
    public double getNowLat(){
        return nowLat;
    }
    public double getNowLon(){
        return nowLon;
    }

}
