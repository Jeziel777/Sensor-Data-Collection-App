package com.example.pav_analytics.sensors

import android.annotation.SuppressLint
import com.example.pav_analytics.util.sensorData
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccelerometerSensor (
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_ACCELEROMETER,
    sensorType = Sensor.TYPE_ACCELEROMETER
) {
    // State to hold the latest sensor data for Compose
    override var sensorDataState by mutableStateOf("")

    @SuppressLint("DefaultLocale")
    override fun handleSensorData(values: FloatArray, currentTimeMillis: Long, nanoTime: Long) {
        val formattedTimestamp = formatTimestamp(currentTimeMillis, nanoTime)
        val data = "ACL,${values[1]},${values[0]},${values[2]},${formattedTimestamp}"
        // Store the latest sensor data
        val x = String.format("%.3f", values[0])
        val y = String.format("%.3f", values[1])
        val z = String.format("%.3f", values[2])
        sensorDataState = "ACCL: x=${x},y=${y},z=${z}"
        sensorData.add(data)
    }
}

class GyroSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_GYROSCOPE,
    sensorType = Sensor.TYPE_GYROSCOPE
) {
    // State to hold the latest sensor data for Compose
    override var sensorDataState by mutableStateOf("")

    @SuppressLint("DefaultLocale")
    override fun handleSensorData(values: FloatArray, currentTimeMillis: Long, nanoTime: Long) {
        val formattedTimestamp = formatTimestamp(currentTimeMillis, nanoTime)
        val data = "GRO,${values[1]},${values[0]},${values[2]},${formattedTimestamp}"
        // Store the latest sensor data
        val x = String.format("%.3f", values[0])
        val y = String.format("%.3f", values[1])
        val z = String.format("%.3f", values[2])
        sensorDataState = "GYRO: x=${x},y=${y},z=${z}"
        sensorData.add(data)
    }
}

class OrientationSensor(
    context: Context
) : AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_COMPASS,
    sensorType = Sensor.TYPE_MAGNETIC_FIELD
) {
    // State to hold the latest sensor data for Compose
    override var sensorDataState by mutableStateOf("")
    private val accelerometerData = FloatArray(3)
    private val magnetometerData = FloatArray(3)

    // Override handleSensorData to combine accelerometer and magnetometer data
    @SuppressLint("DefaultLocale")
    override fun handleSensorData(values: FloatArray, currentTimeMillis: Long, nanoTime: Long) {
        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(values, 0, accelerometerData, 0, values.size)
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(values, 0, magnetometerData, 0, values.size)
            }
        }

        if (accelerometerData.isNotEmpty() && magnetometerData.isNotEmpty()) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            if (SensorManager.getRotationMatrix(
                    rotationMatrix,
                    null,
                    accelerometerData,
                    magnetometerData
                )
            ) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                val formattedTimestamp = formatTimestamp(currentTimeMillis, nanoTime)
                val data = "ORI,${azimuth},${pitch},${roll},${formattedTimestamp}"

                sensorDataState = "ORI: " +
                        "azimuth=${String.format("%.3f", azimuth)}," +
                        "pitch=${String.format("%.3f", pitch)}," +
                        "roll=${String.format("%.3f", roll)}"
                //sensorData.add(data)
            }
        }
    }
}

class MagnetometerSensor(
    context: Context
) : AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_COMPASS,
    sensorType = Sensor.TYPE_MAGNETIC_FIELD
) {
    // State to hold the latest sensor data for Compose
    override var sensorDataState by mutableStateOf("")

    @SuppressLint("DefaultLocale")
    override fun handleSensorData(values: FloatArray, currentTimeMillis: Long, nanoTime: Long) {
        val formattedTimestamp = formatTimestamp(currentTimeMillis, nanoTime)
        val data = "MAG,${values[0]},${values[1]},${values[2]},${formattedTimestamp}"
        // Store the latest sensor data
        val x = String.format("%.3f", values[0])
        val y = String.format("%.3f", values[1])
        val z = String.format("%.3f", values[2])
        sensorDataState = "MAG: x=${x},y=${y},z=${z}"
        //sensorData.add(data)
    }
}

private fun formatTimestamp(timeMillis: Long, nanoAdjustment: Long): String {
    val date = Date(timeMillis)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.getDefault())
    val formattedDate = format.format(date)

    // Replace the comma with a dot in the  timestamp
    val formattedTimestamp = formattedDate.replace(",", ".")

    return formattedTimestamp
}