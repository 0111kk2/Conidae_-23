package com.example.sensortest;

import android.location.Location;

import java.util.Arrays;

public class InductionConidae extends Driver{
    //クラスのインスタンスたち
    private Thread driveThread;
    private Thread shijimiThread;
    private final Shell shell;
    private final Shijimi shijimi;
    private MainActivity mainActivity = null;
    //誘導関連の変数
    private int right=0,left=0;
    private static double g_lat=0.0;
    private static double g_lon=0.0;
    private boolean breaker = false;
    //センサ関連の変数
    private float[] orientationAngles;
    private double goalAzimuth;
    private double distance;
    private double magNorth=0;
    //メンバとしてカメラを入れる
    //コンストラクタ。ShellとShijimiをインスタンス化する。
    InductionConidae(){
        //メインアクティビティは一応インスタンス取得しておく。
        mainActivity = MainActivity.getInstance();
        System.out.println("車を用意するんだえ");
        System.out.println("エンジン始動だえ");
        //Shellのインスタンスを取得
        shell = Shell.getInstance();
        System.out.println("しじみも乗るんだえ");
        //しじみのインスタンスを取得
        shijimi = Shijimi.getInstance();
        System.out.println("しじみ、行くんだえ");
        //しじみのスレッドを開始
        startLog();
        System.out.println("運転開始だえ");
        driveLog("運転開始だえ");
        //driveのスレッドを開始
        drive();
    }
    @Override//制御プログラムをスレッド内で動作させるプログラム
    public void drive() {
        System.out.println("運転するんだえ");
        //スレッドを開始
        driveThread = new Thread(() -> {
            //ゴールまでの距離と方位角を算出
            calculateToGoal();
            //ゴールまでの距離が10m以上のとき
            while (distance>10){
                //目標方位を向く
                induction();
                //induction()の最中にinterruptされたときはbreakerがtrueとなり、スレッドが終了
                if(breaker){
                    System.out.println("方向転換中に終わるんだえ");
                    close();
                    break;
                }
                calculateToGoal();
                try {
                    //ここに進むやつ
                    while(calculate()<30){
                        shell.axel(70,70);
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    //スリープ中にinterruptされたときの処理
                    e.printStackTrace();
                    System.out.println("ドライブ終了だえ");
                    driveLog("ドライブ終了だえ");
                    close();
                    break;
                }
            }
            System.out.println("目的地付近なんだえ");
            driveLog("目的地付近なんだえ");
            close();
        });
        driveThread.start();
    }
    @Override//停止用プログラム。close()メゾットを呼び出す。
    public void quit() {
        System.out.println("もうやめるんだえ");
        driveLog("もうやめるんだえ");
        close();
    }
    //目標方位を向くため、calculate()で算出した制御値をShellから出力するメゾット。
    public void induction(){
        if(shell !=null){
            int outPut;
            do{
                //System.out.println("計算するんだえ");
                //計算を実行
                outPut =calculate();//outPutは、目標方位からの角度差
                System.out.println("出力するんだえ");
                driveLog(left+","+right);
                //Shellの出力関数に計算結果を渡す。
                shell.axel(left, right);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    //interruptされたときの処理
                    e.printStackTrace();
                    breaker = true;
                    break;
                }
            }while (outPut >10);
        }
        else{
            System.out.println("車が無いんだえ");
            driveLog("車が無いんだえ");
        }
    }
    //ゴールまでの距離と目標方位を算出するプログラム
    public void calculateToGoal(){
        float[] results = new float[2];//算出値を格納する配列
        if(shell!=null) {
            //shellのインスタンスから、現在の緯度経度を取得し、ゴールまでの距離と目標方位角を算出する。
            System.out.println("ゴールから逆算するんだえ");
            Location.distanceBetween(shell.getNowLat(), shell.getNowLon(), g_lat, g_lon, results);
        }
        distance = results[0];//距離を書き換え

        //目標方位を書き換え(偏角計算込み)
        if(results[1]+magNorth < 180){
            goalAzimuth = results[1]+magNorth;
        }else{
            goalAzimuth = results[1] - 360 + magNorth;
        }
        System.out.println("ゴールまでの距離:"+distance+"目標方位:"+goalAzimuth+"なんだえ");
        driveLog("ゴールまでの距離:"+distance+"目標方位:"+goalAzimuth+"なんだえ");
    }
    //rightとleftに出力すべき値を算出して代入し、目標方位からの差を絶対値で返す。
    public int calculate(){
        System.out.println("センサ値を取得するんだえ");
        //Shellのセンサのゲッターからセンサ値の取得
        orientationAngles = shell.getOrientation();
        System.out.println(Arrays.toString(orientationAngles));
        System.out.println("計算をするんだえ");
        //現在の方位角を取得
        double nowAzimuth = (double) orientationAngles[0]*180/Math.PI;
        double delta = goalAzimuth - nowAzimuth;
        double phi = 0;
        if ((-360 <= delta) && (delta <= -180)) {//右
            phi = 360 + delta;
            //System.out.println(phi);
        } else if ((0 < delta) && (delta < 180)) {//右
            phi = delta;
            //System.out.println(phi);
        } else if ((-180 < delta) && (delta < 0)) {//左
            phi = delta;
            //System.out.println(phi);
        } else if ((180 <= delta) && (delta <= 360)) {//左
            phi = 180 - delta;
            //System.out.println(phi);
        }
        right = (int) -phi/2;
        left = (int) phi/2;
        return Math.abs((int)phi);
    }
    //ゴール位置をセットするセッター
    public static void setGoal(double lat,double lon){
        g_lat = lat;
        g_lon = lon;
    }
    //ShellとShijimiを停止させるメゾット。
    //この関数は外部から呼び出されることは無いためプライベート
    private void close(){
        System.out.println("止まるんだえ");
        driveLog("止まるんだえ");
        //出力を0にする。
        shell.axel(0,0);
        System.out.println("車を降りるんだえ");
        driveLog("車を降りるんだえ");
        //driveThreadを止める
        if(driveThread!=null){
            driveThread.interrupt();
        }
        //shellのonPause()を呼び出し
        shell.onPause();
        System.out.println("しじみも降りるんだえ");
        driveLog("しじみも降りるんだえ");
        //しじみのスレッドを停止
        if(shijimiThread!=null){
            shijimiThread.interrupt();
        }
    }
    //ドライブ中のログを取る関数。呼び出された際にShijimiの有無を評価する。
    private void driveLog(String str){
        if(shijimi!=null){
            shijimi.driveRecord(str);
        }
    }
    //shijimiのスレッドを開始する関数。
    private void startLog(){
        if (shijimi!=null){
            shijimiThread = new Thread(shijimi);
            shijimiThread.start();
        }
    }

}