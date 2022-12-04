package com.example.kotlindae

import kotlin.properties.Delegates

class InductionKonidae//KShellをインスタンス化
//KShijimiをインスタンス化
//ログのスレッドを開始
    () {
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
        shell = KShell
        println("しじみも乗るんだえ")
        shijimi = KShijimi
        driveLog("しじみ、行くんだえ")
        driveLog("運転開始だえ")
    }
    public fun drive() {
        driveLog("運転するんだえ")
        //スレッドを開始
        driveThread = Thread(Runnable {
            calculateToGoal()
            while (distance!! >10){
                //目標方位を向く
                induction()
                if(breaker){
                    driveLog("方向転換中に終わるんだえ")
                    quit()
                    break
                }
                calculateToGoal()
                while(calculate()<30){
                    try {
                     Thread.sleep(30)
                    }catch (e:InterruptedException){
                        driveLog("ドライブ終了だえ")
                        quit()
                        break
                    }
                }
            }
            if(distance!!<10) {
                driveLog("目的地付近なんだえ")
                quit()
            }
        })
        driveThread.start()
    }
    public fun quit(){
        driveLog("もうやめるんだえ")
        driveThread.interrupt()
        shijimiThread.interrupt()
    }
    private fun induction(){
        do{
            driveLog("出力するんだえ")
            driveLog("{$left},{$right}")
            try {
                Thread.sleep(20)
            }catch (e:InterruptedException){
                breaker = true
                break
            }
        }while (calculate()>10)
    }

    private fun calculateToGoal(){
        var results = FloatArray(2)
        driveLog("ゴールから逆算するんだえ")
        distance = results[0]
        if(results[1]+magNorth < 180){
            goalAzimuth = results[1]+magNorth
        }else{
            goalAzimuth = results[1] - 360 + magNorth
        }
        driveLog("ゴールまでの距離：{$distance}目標方位{$goalAzimuth}なんだえ")
    }
    private fun calculate(): Int {
        driveLog("センサ値を取得するんだえ")
        return 30
    }
    private fun driveLog(str:String){
        print(String)
    }
    private fun startLog(){

    }
}