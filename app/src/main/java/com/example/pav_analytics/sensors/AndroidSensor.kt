package com.example.pav_analytics.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi

abstract class AndroidSensor(
    private val context: Context,
    private val sensorFeature: String,
    sensorType: Int
) : MeasurableSensor(sensorType), SensorEventListener {

    override val doesSensorExist: Boolean
        get() = context.packageManager.hasSystemFeature(sensorFeature)

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun startListening() {
        startListening(200) // Default to 200 Hz
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun startListening(frequencyHz: Int) {
        if (!doesSensorExist) {
            return
        }
        if (!::sensorManager.isInitialized && sensor == null) {
            sensorManager = context.getSystemService(SensorManager::class.java) as SensorManager
            sensor = sensorManager.getDefaultSensor(sensorType)
        }
        sensor?.let {
            val desiredDelayMicroseconds = (1000000 / frequencyHz).toInt()
            sensorManager.registerListener(this, it, desiredDelayMicroseconds)
        }
    }

    override fun stopListening() {
        if (!doesSensorExist || !::sensorManager.isInitialized) {
            return
        }
        sensor?.let {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!doesSensorExist) {
            return
        }
        if (event?.sensor?.type == sensorType) {
            // Get the current time in milliseconds and nanoseconds
            val currentTimeMillis = System.currentTimeMillis()
            val nanoTime = System.nanoTime()
            handleSensorData(event.values,  currentTimeMillis, nanoTime)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    abstract fun handleSensorData(values: FloatArray, currentTimeMillis: Long, nanoTime: Long)

}