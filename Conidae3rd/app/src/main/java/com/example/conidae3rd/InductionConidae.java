package com.example.conidae3rd;

import android.location.Location;

import java.util.Arrays;

public class InductionConidae extends Driver{
    //クラスのインスタンスたち
    private Thread driveThread;
    private final Shell shell;
    //誘導関連の変数
    private int right=0,left=0;
    private static double g_lat=0.0;
    private static double g_lon=0.0;
    private boolean breaker = false;
    //センサ関連の変数
    private float[] orientationAngles;
    private double goalAzimuth;
    private double distance;
    //メンバとしてカメラを入れる
    InductionConidae(){
        System.out.println("車を用意するんだえ");
        System.out.println("エンジン始動だえ");
        //Shellをインスタンス化
        shell = new Shell();
        System.out.println("しじみも乗るんだえ");
        //しじみインスタンス化
        System.out.println("しじみ、行くんだえ");
        //しじみのスレッドを開始
        System.out.println("運転開始だえ");
        //driveのスレッドを開始
        drive();
    }

    @Override
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
                //途中でinterruptされたとき二はbreakerがtrueとなり、スレッドが終了
                if(breaker){
                    System.out.println("方向転換中に終わるんだえ");
                    close();
                    break;
                }
                calculateToGoal();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("ドライブ終了だえ");
                    close();
                    break;
                }
            }
            System.out.println("目的地付近なんだえ");
            close();
        });
        driveThread.start();
    }

    @Override
    public void quit() {
        System.out.println("もうやめるんだえ");
        //driveThreadを止める
        if(driveThread!=null){
            driveThread.interrupt();
        }
    }

    public void induction(){
        if(shell !=null){
            int outPut;
            do{
                //System.out.println("計算するんだえ");
                //計算を実行
                outPut =calculate();
                System.out.println("出力するんだえ");
                //Shellの出力関数に計算結果を渡す。
                shell.axel(left, right);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    breaker = true;
                    break;
                }
            }while (outPut >10);
        }
        else{
            System.out.println("車が無いんだえ");
        }
    }
    public void calculateToGoal(){
        float[] results = new float[2];
        if(shell!=null) {
            System.out.println("ゴールから逆算するんだえ");
            Location.distanceBetween(shell.getNowLat(), shell.getNowLon(), g_lat, g_lon, results);
        }
        distance = results[0];
        goalAzimuth = results[1];
        System.out.println("ゴールまでの距離:"+distance+"目標方位:"+goalAzimuth+"なんだえ");
    }
    public int calculate(){
        System.out.println("センサ値を取得するんだえ");
        //Shellのセンサのゲッターからセンサ値の取得
        orientationAngles = shell.getOrientation();
        System.out.println(Arrays.toString(orientationAngles));
        System.out.println("計算をするんだえ");
        double nowAzimuth = (double) orientationAngles[0]*180/Math.PI;
        double delta = goalAzimuth - nowAzimuth;
        double phi = 0;
        if ((-360 < delta) && (delta < -180)) {//右
            phi = 360 + delta;
            //System.out.println(phi);
            right = (int) -phi / 2;
            left =(int) phi / 2;

        } else if ((0 < delta) && (delta < 180)) {//右
            phi = delta;
            //System.out.println(phi);
            right = (int) -phi / 2;
            left =(int) phi / 2;

        } else if ((-180 < delta) && (delta < 0)) {//左
            phi = -delta;
            //System.out.println(phi);
            right = (int) phi / 2;
            left =(int) -phi / 2;

        } else if ((180 < delta) && (delta < 360)) {//左
            phi = delta - 180;
            //System.out.println(phi);
            right = (int) phi / 2;
            left =(int) -phi / 2;
        }
        return (int)phi;
    }
    //セッター
    public static void setGoal(double lat,double lon){
        g_lat = lat;
        g_lon = lon;
    }
    //この関数は外部から呼び出されることは無いためプライベート
    private void close(){
        System.out.println("止まるんだえ");
        //出力を0にする。
        shell.axel(0,0);
        System.out.println("車を降りるんだえ");
        //shellのonPause()を呼び出し
        shell.onPause();
        System.out.println("しじみも降りるんだえ");
        //しじみのスレッドを停止
    }
}