package com.example.sensortest;

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
    //姿勢角関連。
    static SensorManager sensorManager = null;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] rotationFinal = new float[9];
    private final float[] orientationAngles = new float[3];
    //GPS関連
    private LocationManager locationManager;
    private double nowLat;
    private double nowLon;
    private MainActivity mContext = MainActivity.getInstance();
    //Bluetooth関連
    private BluetoothCommunication blue;
    private int mRight;
    private int mLeft;
    //コンストラクタ
    private Shell(){
        System.out.println("ブルルルル...(エンジン音)");
        //Bluetooth通信のクラスをインスタンス化
        blue = new BluetoothCommunication("ESP32test");
        //blue.StartBluetoothConnection("ESP32test");
        System.out.println("位置情報シュトクカイシ");
        locationStart();
        System.out.println("センサ値シュトクカイシ");
        onResume();
    }
    //Shellクラスのインスタンスを取得する関数。この記述により、プログラム全体でShellクラスのインスタンスは1つのみ
    //となり、ConidaeとShijimiは同じインスタンスにアクセスすることになる。(詳しくはシングルトンパターンで検索)
    public static Shell getInstance(){
        return ShellInstanceHolder.INSTANCE;
    }
    //=========================================センサと姿勢角関連
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
    public void onAccuracyChanged(Sensor sensor, int sensorAccuracy) {
        if(sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
            if(sensorAccuracy==0){
                mContext.setTxtAccuracy("UNRELIABLE");
            }
            else if(sensorAccuracy==1){
                mContext.setTxtAccuracy("低精度");
            }
            else if (sensorAccuracy==2){
                mContext.setTxtAccuracy("通常精度");
            }
            else if (sensorAccuracy==3){
                mContext.setTxtAccuracy("高精度");
            }
            else{
                mContext.setTxtAccuracy("接続無し");
            }
        }
    }
    public void onResume(){
        sensorManager= (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer != null){
            sensorManager.registerListener(this,accelerometer,
                    SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magneticField != null){
            sensorManager.registerListener(this,magneticField,
                    SensorManager.SENSOR_DELAY_GAME,SensorManager.SENSOR_DELAY_UI);
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
        mContext.setTxtSpeak(orientationAngles[0]/Math.PI*180+"");
    }
    //============================================location関連
    @Override
    public void onLocationChanged(@NonNull Location location) {
        nowLat = location.getLatitude();
        nowLon = location.getLongitude();
        mContext.setTxtLat("緯度"+nowLat);
        mContext.setTxtLon("経度"+nowLon);
    }
    public void axel(int left,int right){
        mRight = right;
        mLeft = left;
        if(right>=90){
            mRight=90;
        }
        else if(right<=-180){
            mRight=-90;
        }
        if(left>=90){
            mLeft=90;
        }
        else if(left<=-90){
            mLeft=-90;
        }
        //出力を左右反転させたい場合はここの下にあるやつの+-を逆転させる
        System.out.println("シュツリョクシマス"+mLeft+":"+mRight);
        //blue.sendData((90+mLeft)+","+(90+mRight)+";");
        mContext.runOnUiThread(() -> {
            mContext.setTxtLeft(""+(90+mLeft));
            mContext.setTxtRight(""+(90+mRight));
        });
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

        if (ContextCompat.checkSelfPermission((Context) mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            Log.d("debug", "checkSelfPermission false");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
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
    //シングルトンにするためのインナークラス
    public static class ShellInstanceHolder{
        private static final Shell INSTANCE = new Shell();
    }
}
