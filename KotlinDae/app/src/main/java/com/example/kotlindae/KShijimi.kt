package com.example.kotlindae

import android.os.Environment
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class KShijimi(kShell: KShell):Runnable{
    private var shell = kShell

    //スレッド内にて実行させる処理。今後実装予定。
    override fun run() {
        driveRecord("記録開始だミ!")
        while (true) {
            //センサ値の記録
            sensorRecord()
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                //スレッドを停止させる指示が出たときの処理。
                e.printStackTrace()
                break
            }
        }
    }
    fun quit(){
        closeAllFiles()
    }
    //センサ値を記録する関数。
    private fun sensorRecord() {
        println("センサ値を記録だミ")
        //センサ値はshellインスタンスからゲッターにて呼び出す。
        if((shell.nowLat!=null)&&(shell.nowLon!=null)){
            val logStr = "${shell.nowLat},${shell.nowLon}"
            writeLog(logStr, true, sensorFw)
        }
    }
    //ドライブの様子を記録する関数。引数に書き込みたい文字列を渡す。
    fun driveRecord(str: String?) {
        writeLog(str!!, true, driveFw)
    }


    //クラスの静的メンバ．参照されるファイルがインスタンス化によって違うものにならないようにする．新しいファイルはresumeが呼び出されると作られる．
    companion object{
        private lateinit var sensorFw:BufferedWriter
        private lateinit var driveFw:BufferedWriter

        init {
            if(isExternalStorageWritable()){
                initFiles()
            }
        }
        private fun initFiles(){
            println("ファイルを作るミ")
            val stamp = getTimeStamp()
            val sensorLogFileName = "SensorLog_$stamp.txt"
            val driveLogFileName = "DriveLog_$stamp.txt"
            //ドキュメントディレクトリ内にファイルを作成。この場合はgetExternalStoragePublicDirectoryを利用。
            val sensorLogFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                sensorLogFileName
            )
            val driveLogFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                driveLogFileName
            )
            try {
                //ファイル書き込み用のFileWriterを作成
                sensorFw = BufferedWriter(FileWriter(sensorLogFile, true))
                driveFw = BufferedWriter(FileWriter(driveLogFile, true))
            } catch (e: IOException) {
                println("ファイルが作れないんだミ！")
                e.printStackTrace()
            }
        }
        private fun getTimeStamp():String{
            val cal = Calendar.getInstance()
            return (cal[Calendar.YEAR].toString() + "-"
                    + (cal[Calendar.MONTH] + 1) + "-"
                    + cal[Calendar.DAY_OF_MONTH] + "-"
                    + cal[Calendar.HOUR_OF_DAY] + "-"
                    + cal[Calendar.MINUTE] + "-"
                    + cal[Calendar.SECOND] + "-"
                    + cal[Calendar.MILLISECOND])
        }

        //ファイルに書き込みをする関数。引数の意味としては、(書き込む内容,タイムスタンプを使うか否か,書き込み先のFileWriter)
        private fun writeLog(str: String, useTimeStamp: Boolean, fw: BufferedWriter) {
            if (useTimeStamp) {
                try {
                    fw.write(
                        """
                    ${getTimeStamp()},$str
                    
                    """.trimIndent()
                    ) //渡されたファイルに書き込み
                } catch (e: IOException) {
                    e.printStackTrace()
                    try {
                        fw.close()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                        println("書き込み失敗だミ!")
                    }
                }
            } else {
                try {
                    fw.write(str) //渡されたファイルに書き込み
                } catch (e: IOException) {
                    e.printStackTrace()
                    try {
                        fw.close()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                        println("書き込み失敗だミ!")
                    }
                }
            }
        }

        //作成したファイルをすべて閉じる関数。必ず呼び出さなければならない。
        fun closeAllFiles() {
            try {
                sensorFw.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    sensorFw.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                driveFw.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    driveFw.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        private fun isExternalStorageWritable():Boolean{
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
        }
    }
}