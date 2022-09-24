package com.example.conidae3rd;

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
    private static String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH
    };
    //Bluetooth関連
    private Context mContext = MainActivity.getInstance();//アクティビティを開始したりする際に色々と使う。
    private BluetoothAdapter mBluetoothAdapter = null;//Bluetooth関連の動作を行う際に必ず要る。
    BluetoothSocket mBluetoothSocket = null;
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;
    private int count = 0;
    private String connectDevicename=null;
    BluetoothCommunication(String deviceName){
        connectDevicename = deviceName;
        //if(connectDevicename!=null){
          //  StartBluetoothConnection(connectDevicename);
        //}
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
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("パーミッションがありません");
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                ActivityCompat.requestPermissions((Activity) mContext,permissions,1000);
            }
            mContext.startActivity(enableBtIntent);
        }
        connectDevice(deviceName);
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
            } catch (Exception connectIOException) {
                System.out.println("何らかの問題が発生したんだえ");
                connectIOException.printStackTrace();
                try {
                    mBluetoothSocket.close();
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
                System.out.println("送れないんだえ");
                writeIOException.printStackTrace();
                System.out.println("もう一度接続を試みるんだえ");
                connectDevice(connectDevicename);
            }
        }
        else {
            connectDevice(connectDevicename);
        }
    }
}
