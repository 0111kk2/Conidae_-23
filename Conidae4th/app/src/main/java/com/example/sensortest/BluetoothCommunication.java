package com.example.sensortest;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothCommunication {
    //使用するパーミッションの宣言
    private final static String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH
    };
    //Bluetooth関連
    private final Context mContext = MainActivity.getInstance();//アクティビティを開始したりする際に色々と使う。
    private BluetoothAdapter mBluetoothAdapter = null;//Bluetooth関連の動作を行う際に必ず要る。
    BluetoothSocket mBluetoothSocket = null;
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;
    private int count = 0;
    private final String connectDeviceName;
    private Shijimi shijimi;
    private MainActivity mainActivity = MainActivity.getInstance();
    BluetoothCommunication(String deviceName){
        connectDeviceName = deviceName;
        shijimi = Shijimi.getInstance();
    }

    public void StartBluetoothConnection(String deviceName) {
        System.out.println("Bluetoothサポート確認");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            System.out.println("Bluetoothがサポートされていません");
            return;
        }
        mBluetoothSocket = null;
        mOutputStream = null;
        mInputStream = null;
        //Bluetoothがオンになるようにする。
        if (!mBluetoothAdapter.isEnabled()) {
            System.out.println("Bluetoothがオンになっていません");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            checkPermission();
            mContext.startActivity(enableBtIntent);
        }
        connectDevice(deviceName);
    }

    private void connectDevice(String connectDeviceName) {
        checkPermission();
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
            System.out.println("ソケットだえ"+mBluetoothSocket.toString());
        } catch (Exception e) {
            System.out.println("やばいんだえ");
            e.printStackTrace();
            if(count<3){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println("もう一度トライするんだえ");
                count++;
                this.connectDevice(connectDeviceName);
            }
            else{
                System.out.println("諦めるんだえ");
            }
        }
        if (mBluetoothSocket != null) {
            count = 0;
            System.out.println("ソケットが取得できたんだえ");
            try {
                mBluetoothSocket.connect();
                mOutputStream = mBluetoothSocket.getOutputStream();
                mInputStream = mBluetoothSocket.getInputStream();
                System.out.println("接続ができたんだえ");
                mainActivity.setTxtBlue("接続済");

            } catch (Exception connectIOException) {
                System.out.println("何らかの問題が発生したんだえ");
                driveLog("何らかの問題が発生したんだえ");
                connectIOException.printStackTrace();
                mainActivity.setTxtBlue("未接続");
                try {
                    if(mBluetoothSocket!=null){
                        mBluetoothSocket.close();
                    }
                } catch (IOException closeIOException) {
                    closeIOException.printStackTrace();
                }
                mBluetoothSocket = null;
            }
        }
    }
    public void sendData(String data) {
        System.out.println("データを送るんだえ");
        if(mOutputStream != null) {
            byte[] sendBytes = data.getBytes();
            try {
                mOutputStream.write(sendBytes);
            } catch (IOException writeIOException) {
                mainActivity.setTxtBlue("未接続");
                driveLog("送信できないんだえ");
                System.out.println("送れないんだえ");
                writeIOException.printStackTrace();
                driveLog("もう一度接続を試みるんだえ");
                System.out.println("もう一度接続を試みるんだえ");
                connectDevice(connectDeviceName);
            }
        }
        else {
            connectDevice(connectDeviceName);
        }
    }
    private void driveLog(String str){
        if(shijimi!=null){
            shijimi.driveRecord(str);
        }
        else{
            shijimi = Shijimi.getInstance();
        }
    }

    private void checkPermission(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("パーミッションがありません");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions((Activity) mContext,permissions,1000);
        }
    }
}
