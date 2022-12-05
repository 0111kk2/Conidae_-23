package com.example.kotlindae

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class KShell(bluetooth:BluetoothKommunication,context: Context) :SensorEventListener, LocationListener{
    //姿勢角関連
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)
    //GPS関連
    private lateinit var locationManager: LocationManager
    var nowLat:Double? = null
    var nowLon:Double? = null
    private var mContext:Context = context
    //Bluetooth関連
    private var blue:BluetoothKommunication = bluetooth
    private var mRight = 0
    private var mLeft = 0
    init {
        locationStart()
        onResume()
    }
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        updateOrientationAngles()
    }
    private fun updateOrientationAngles(){
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        var rotationFinal = FloatArray(9)
        //軸の変更
        SensorManager.remapCoordinateSystem(rotationMatrix,SensorManager.AXIS_Z,SensorManager.AXIS_MINUS_X,rotationFinal)
        // "mRotationMatrix" now has up-to-date information.
        SensorManager.getOrientation(rotationFinal, orientationAngles)
        // "mOrientationAngles" now has up-to-date information.
    }
    override fun onAccuracyChanged(Sensor: Sensor?, p1: Int) {
        TODO("Not yet implemented")
    }

    private fun locationStart(){
        locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.d("debug","location manager Enabled")
        }else{
            var settingsIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            mContext.startActivity(settingsIntent)
        }
        if(ContextCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                mContext as Activity,
                mutableListOf(ACCESS_FINE_LOCATION).toTypedArray(),1000)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1f, this)
    }

    fun onResume(){
        sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME,SensorManager.SENSOR_DELAY_UI)
        val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this,magneticField,SensorManager.SENSOR_DELAY_GAME,SensorManager.SENSOR_DELAY_UI)
    }

    override fun onLocationChanged(location: Location) {
        nowLat = location.latitude
        nowLon = location.longitude
    }
    fun onPause(){
        sensorManager.unregisterListener(this)
    }
    fun axel(left:Int,right:Int){
        mRight = 90-right
        mLeft = left+90
        blue.sendData("{$mLeft},{$mRight}")
    }
}
