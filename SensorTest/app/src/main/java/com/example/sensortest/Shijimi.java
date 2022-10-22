package com.example.sensortest;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class Shijimi implements Runnable{
    private FileWriter sensorFw = null;
    private FileWriter driveFw = null;
    private Shell shell=null;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Shijimi(){
        if(isExternalStorageWritable()){//外部ストレージに読み書きできるかを検証
            InitFiles();//ファイルの作成及びFileWriterの取得
             shell = Shell.getInstance();
        }
    }

    private void InitFiles(){//ファイルの作成及びFileWriterの取得を行う関数。
        System.out.println("ファイルを作るミ");
        String stamp = getTimeStamp();//タイムスタンプを取得し、ファイル名に追加する。
        String sensorLogFileName = "SensorLog_"+stamp+".txt";
        String driveLogFileName = "DriveLog_"+stamp+".txt";
        //ドキュメントディレクトリ内にファイルを作成。この場合はgetExternalStoragePublicDirectoryを利用。
        File sensorLogFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), sensorLogFileName);
        File driveLogFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), driveLogFileName);
        try {
            //ファイル書き込み用のFileWriterを作成
            sensorFw = new FileWriter(sensorLogFile,true);
            driveFw = new FileWriter(driveLogFile,true);
        } catch (IOException e) {
            System.out.println("ファイルが作れないんだミ！");
            e.printStackTrace();
        }
    }
    //タイムスタンプ取得用の関数。
    private String getTimeStamp(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR)+"-"
                +(cal.get(Calendar.MONTH)+1)+"-"
                +cal.get(Calendar.DAY_OF_MONTH)+"-"
                +cal.get(Calendar.HOUR_OF_DAY)+"-"
                +cal.get(Calendar.MINUTE)+"-"
                +cal.get(Calendar.SECOND)+"-"
                +cal.get(Calendar.MILLISECOND);
    }
    //ファイルに書き込みをする関数。引数の意味としては、(書き込む内容,タイムスタンプを使うか否か,書き込み先のFileWriter)
    private void writeLog(String str,boolean useTimeStamp,FileWriter fw){
        if(useTimeStamp){
            try {
                fw.write(getTimeStamp()+","+str+"\n");//渡されたファイルに書き込み
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("書き込み失敗だミ!");
                }
            }
        }
        else{
            try {
                fw.write(str);//渡されたファイルに書き込み
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("書き込み失敗だミ!");
                }
            }
        }
    }
    //ファイル書き込みができるか確認する関数
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    //publicで宣言している関数たち。外部から呼び出せる。
    //センサ値を記録する関数。
    public void sensorRecord(){
        System.out.println("センサ値を記録だミ");
        //センサ値はshellインスタンスからゲッターにて呼び出す。
        if(shell!=null){
            String logStr =shell.getNowLat()+","+shell.getNowLon();
            if(sensorFw!=null){
                writeLog(logStr,true,sensorFw);
            }
        }
        else{
            shell = Shell.getInstance();
        }
    }
    //ドライブの様子を記録する関数。引数に書き込みたい文字列を渡す。
    public void driveRecord(String str){
        writeLog(str,true,driveFw);
    }
    //作成したファイルをすべて閉じる関数。必ず呼び出さなければならない。
    public void closeAllFiles(){
        if(sensorFw!=null){
            try {
                sensorFw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    sensorFw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(driveFw!=null){
            try {
                driveFw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    driveFw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //スレッド内にて実行させる処理。今後実装予定。
    @Override
    public void run() {
        driveRecord("記録開始だミ!");
        while (true){
            //センサ値の記録
            sensorRecord();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //スレッドを停止させる指示が出たときの処理。
                e.printStackTrace();
                closeAllFiles();
                break;
            }
        }
    }
    //Shijimiのインスタンスを保持するクラス
    public static Shijimi getInstance(){
        return ShijimiInstanceHolder.INSTANCE;
    }
    //シングルトンにするためのインナークラス
    public static class ShijimiInstanceHolder {
        private static final Shijimi INSTANCE = new Shijimi();
    }
}
