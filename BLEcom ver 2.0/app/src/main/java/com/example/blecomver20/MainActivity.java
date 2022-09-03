package com.example.blecomver20;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String CONNECT_DEVICE_NONE = "NONE";
    private static final String CONNECT_STATUS_CONNECT = "接続";
    private static final String CONNECT_DISCONNECT_CONNECT = "未接続";

    private BluetoothAdapter mBluetoothAdapter;
    private ActivityResultLauncher<Intent> mActivityResultLauncher;

    private Button mAddDeviceButton;
    private Spinner mConnectDeviceSpinner;
    private ArrayAdapter<String> mConnectDeviceAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkDeviceSupportBluetooth();
        initBluetoothAdapter();
        showBluetoothDisabledAlertDialogAndFinishApp();
        initAddDeviceButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initConnectDeviceSpinner();
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




}