package com.example.kotlindae

import android.content.Context
import android.location.Location
import kotlin.math.abs

class InductionKonidae//KShellをインスタンス化
//KShijimiをインスタンス化
//ログのスレッドを開始
    (BluetoothKommunication: BluetoothKommunication,context: Context) {
    //取り合えずインスタンス化が必要なスレッドクラス、shell,shijimiを宣言。lateinitとついているのはあとで初期化をするという意味。
    // kotlinはvarと宣言したら代入した値に対して型を推定してくれるが、lateinitの場合は型宣言が必要な模様。
    private lateinit var driveThread: Thread
    private lateinit var shijimiThread: Thread
    private var shell: KShell
    private var shijimi: KShijimi
    //誘導関連の変数
    private var right = 0
    private var left = 0
    private var goalLat:Double = 0.0
    private var goalLon:Double = 0.0
    private var breaker = false
    //センサ関連の変数
    private var orientationAngles = FloatArray(3)
    private var goalAzimuth:Double? = null
    private var distance:Float? = null
    private var magNorth = 0.0

    init {
        println("車を用意するんだえ")
        println("エンジン始動だえ")
        shell = KShell(BluetoothKommunication,context)
        println("しじみも乗るんだえ")
        shijimi = KShijimi(shell)
        driveLog("しじみ、行くんだえ")
        startLog()
        driveLog("運転開始だえ")
        drive()
    }
    fun drive() {
        driveLog("運転するんだえ")
        //スレッドを開始
        driveThread = Thread {
            calculateToGoal()
            while (distance!! > 10) {
                //目標方位を向く
                induction()
                if (breaker) {
                    driveLog("方向転換中に終わるんだえ")
                    quit()
                    break
                }
                //前進するプログラムはここから
                calculateToGoal()
                try {
                    while (calculate() < 30) {
                        shell.axel(70, 70)
                        Thread.sleep(50)
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    driveLog("ドライブ終了だえ")
                    quit()
                    break
                }
            }
            if (distance!! < 10) {
                driveLog("目的地付近なんだえ")
                quit()
            }
        }
        driveThread.start()
    }
    fun quit(){
        driveLog("もうやめるんだえ")
        //スレッドを止める
        driveThread.interrupt()
        shijimiThread.interrupt()
        driveLog("止まるんだえ")
        shell.axel(0,0)
        driveLog("車を降りるんだえ")
        //shellのonPause()を呼び出し
        shell.onPause()
        driveLog("しじみも降りるんだえ")
    }
    private fun induction(){
        do{
            driveLog("出力するんだえ")
            driveLog("$left,$right")
            shell.axel(left,right)
            try {
                Thread.sleep(20)
            }catch (e:InterruptedException){
                e.printStackTrace()
                breaker = true
                break
            }
        }while (calculate()>10)
    }

    private fun calculateToGoal(){
        val results = FloatArray(2)
        driveLog("ゴールから逆算するんだえ")
        shell.nowLat?.let { shell.nowLon?.let { it1 ->
            Location.distanceBetween(it,
                it1,goalLat,goalLon,results)
        } }
        distance = results[0]
        goalAzimuth = if(results[1]+magNorth < 180){
            results[1]+magNorth
        }else{
            results[1] - 360 + magNorth
        }

        driveLog("ゴールまでの距離：{$distance}目標方位{$goalAzimuth}なんだえ")
    }
    private fun calculate(): Int {
        driveLog("センサ値を取得するんだえ")
        //センサ値の取得
        orientationAngles = shell.orientationAngles
        driveLog("$orientationAngles")
        //現在の方位角を取得
        val nowAzimuth = orientationAngles[0]*180/Math.PI
        val delta = goalAzimuth?.minus(nowAzimuth)
        var phi = 0.0
        if((-360<= delta!!)&&(delta<=-180)){
            phi = 360+delta
        }else if ((0<delta)&&(delta<180)){
            phi = delta
        }else if((-180<delta)&&(delta<0)) {
            phi = delta
        }else if((180<=delta)&&(delta<=360)){
            phi = 180 - delta
        }
        right = (-phi/2).toInt()
        left = (phi/2).toInt()
        return abs(phi).toInt()
    }
    private fun driveLog(str:String){
        println(str)
        shijimi.driveRecord(str)
    }
    private fun startLog(){
        shijimiThread = Thread(shijimi)
        shijimiThread.start()
    }
    fun setGoals(latitude:Double,longitude:Double){
        goalLat = latitude
        goalLon = longitude
    }
}