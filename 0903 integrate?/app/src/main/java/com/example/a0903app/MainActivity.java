package com.example.a0903app;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    LocationManager locationManager;

    private BluetoothAdapter mBluetoothAdapter;
    private ActivityResultLauncher<Intent> mActivityResultLauncher;
    private Button mAddDeviceButton;
    private Spinner mConnectDeviceSpinner;
    private ArrayAdapter<String> mConnectDeviceAdapter;

    private TextView mConnectStatusTextView;
    private Button mConnectButton;
    BluetoothSocket mBluetoothSocket;

    private EditText mSendDataEditText;
    private Button mSendButton;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private TextView mReceiveDataTextView;
    private Thread mReceiveThread;
    private ReceiveRunnable mReceiveRunnable;

    private static final String TAG = "MainActivity";
    private static final String CONNECT_DEVICE_NONE = "NONE";
    private static final String CONNECT_STATUS_CONNECT = "接続";
    private static final String CONNECT_DISCONNECT_CONNECT = "未接続";

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
    double azimuth1 = 0;
    double azimuth2 = 0;
    double deltaarg = 0;
    double phi = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    boolean inter;


    private void checkPermissions(){
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else if (permission2 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_LOCATION,
                    1
            );
        }
    }
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
        azimuth1 = results[1];
        System.out.println(disbet);
        System.out.println(azimuth1);
    }

    //main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        locationStart();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        checkDeviceSupportBluetooth();
        initBluetoothAdapter();
        initAddDeviceButton();
        initConnectButton();
        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(view -> {
            try {
                cal_induction();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Button btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(view -> inter = false);
    }
    @Override
    protected void onResume(){
        super.onResume();
        //System.out.println(disbet);
        //System.out.println(azimuth);
        initConnectDeviceSpinner();
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
        disconnectBluetooth();
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

        azimuth2 = (double) orientationAngles[0];

        //テキストビューに値を代入する。他で使う場合は削除
        //System.out.println(orientationAngles[0]*180/Math.PI);//方位角
        //System.out.println(orientationAngles[1]*180/Math.PI);//前後の傾き
        //System.out.println(orientationAngles[2]*180/Math.PI);//左右の傾き


    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void checkDeviceSupportBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth.");
            finish();
        }
    }

    private void initBluetoothAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        int resultCode = result.getResultCode();

                        switch (resultCode) {
                            case RESULT_OK:
                                break;
                            case RESULT_CANCELED:
                                showBluetoothDisabledAlertDialogAndFinishApp();
                                break;
                            case RESULT_FIRST_USER:
                            default:
                                break;
                        }
                    }
                });

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivityResultLauncher.launch(enableBtIntent);
        }
    }

    private void showBluetoothDisabledAlertDialogAndFinishApp() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("BluetoothがOFFになっています。")
                .setMessage("BluetoothをONにしてアプリを再起動してください。")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void initAddDeviceButton() {
        mAddDeviceButton = findViewById(R.id.add_device_button);
        mAddDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
    }


    private void initConnectButton() {
        mConnectStatusTextView = findViewById(R.id.connect_status_text_view);
        mBluetoothSocket = null;
        mOutputStream = null;
        mInputStream = null;
        mConnectButton = findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedItemPosition = mConnectDeviceSpinner.getSelectedItemPosition();
                String deviceName = mConnectDeviceAdapter.getItem(selectedItemPosition);

                if(deviceName.equals(CONNECT_DEVICE_NONE)) {
                    Log.e(TAG,"device is NONE");
                } else {
                    connectDevice(deviceName);
                }
            }
        });
    }

    private void connectDevice(String connectDeviceName) {
        BluetoothDevice connectBluetoothDevice = null;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice bluetoothDevice : pairedDevices) {
                if (connectDeviceName.equals(bluetoothDevice.getName())) {
                    connectBluetoothDevice = bluetoothDevice;
                    break;
                }
            }
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try {
            mBluetoothSocket = connectBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException createIOException) {
            createIOException.printStackTrace();
        }
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.connect();
                mConnectStatusTextView.setText(CONNECT_STATUS_CONNECT);
                mOutputStream = mBluetoothSocket.getOutputStream();
                mInputStream = mBluetoothSocket.getInputStream();
                startReceiveTask();
            } catch (IOException connectIOException) {
                connectIOException.printStackTrace();
                try {
                    mBluetoothSocket.close();
                } catch (IOException closeIOException) {
                    closeIOException.printStackTrace();
                }
                mBluetoothSocket = null;
                Log.e(TAG, "Bluetooth connect failed.");
            }
        }
    }


    private void sendData(int right,int left) {
        int mright = 90-right;
        int mleft = 90+left;
        String sendString = String.format("%d,%d;",mright,mleft);
        if(mOutputStream != null) {
            byte[] sendBytes = sendString.getBytes();
            try {
                mOutputStream.write(sendBytes);
            } catch (IOException writeIOException) {
                writeIOException.printStackTrace();
            }
        }
    }

    private void startReceiveTask() {
        if(mReceiveRunnable != null) {
            mReceiveRunnable.shutdown();
        }
        mReceiveRunnable = new ReceiveRunnable();
        mReceiveThread = new Thread(mReceiveRunnable);
        mReceiveThread.start();
    }


    private void initConnectDeviceSpinner() {
        mConnectDeviceSpinner = findViewById(R.id.connect_device_spinner);
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices != null) {
            mConnectDeviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);

            if(pairedDevices.isEmpty()) {
                mConnectDeviceAdapter.add(CONNECT_DEVICE_NONE);
            } else {
                for (BluetoothDevice bluetoothDevice : pairedDevices) {
                    String deviceName = bluetoothDevice.getName();
                    mConnectDeviceAdapter.add(deviceName);
                }
            }
        }
        mConnectDeviceSpinner.setAdapter(mConnectDeviceAdapter);
    }

    private void disconnectBluetooth() {
        if(mReceiveRunnable != null) {
            mReceiveRunnable.shutdown();
            mReceiveRunnable = null;
        }
        if(mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException closeIOException) {
                closeIOException.printStackTrace();
            }
            mOutputStream = null;
        }
        if(mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException closeIOException) {
                closeIOException.printStackTrace();
            }
            mInputStream = null;
        }
        if(mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (IOException closeIOException) {
                closeIOException.printStackTrace();
            }
            mBluetoothSocket = null;
        }
        mConnectStatusTextView.setText(CONNECT_DISCONNECT_CONNECT);
    }

    class ReceiveRunnable implements Runnable {
        private boolean mIsKeepRunning;

        @Override
        public synchronized void run() {
            mIsKeepRunning = true;
            while(mIsKeepRunning) {
                receiveData();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException threadSleepInterruptedException) {
                    threadSleepInterruptedException.printStackTrace();
                }
            }
        }

        public void shutdown() {
            mIsKeepRunning = false;
        }

        private void receiveData() {
            if(mInputStream != null) {
                byte[] receiveData = new byte[256];
                int size;
                try {
                    size = mInputStream.read(receiveData);
                    if(size > 0) {
                        receiveData[size] = '\0';
                        String receiveString = new String(receiveData, java.nio.charset.StandardCharsets.UTF_8);
                        mReceiveDataTextView.setText(receiveString);
                    }
                } catch (IOException readIOException) {
                    readIOException.printStackTrace();
                }
            }
        }
    }


    public void cal_induction() throws InterruptedException {//駆動の処理をするため角度で分岐
        inter = true;
        deltaarg = azimuth1 - azimuth2;
        while (disbet != 0) {
            do {
                if ((-360 < deltaarg) && (deltaarg < -180)) {//右
                    phi = 360 + deltaarg;
                    System.out.println(phi);
                    sendData((int) phi / 2, (int) -phi / 2);

                } else if ((0 < deltaarg) && (deltaarg < 180)) {//右
                    phi = deltaarg;
                    System.out.println(phi);
                    sendData((int) phi / 2, (int) -phi / 2);

                } else if ((-180 < deltaarg) && (deltaarg < 0)) {//左
                    phi = -deltaarg;
                    System.out.println(phi);
                    sendData((int) -phi / 2, (int) phi / 2);

                } else if ((180 < deltaarg) && (deltaarg < 360)) {//左
                    phi = deltaarg - 180;
                    System.out.println(phi);
                    sendData((int) -phi / 2, (int) phi / 2);
                }
                if(!inter){
                    break;
                }
                Thread.sleep(30);
            }while (phi >= 10) ;
            sendData(50, 50);
            Thread.sleep(10000);
        }
    }




}









