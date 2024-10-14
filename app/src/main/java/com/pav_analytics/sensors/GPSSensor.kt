package com.pav_analytics.sensors

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pav_analytics.util.sensorData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GPSSensor(
    private val context: Context
) : MeasurableSensor(sensorType = -1), LocationListener {

    override var sensorDataState by mutableStateOf("Waiting for GPS data...")

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override val doesSensorExist: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    private var isListening = false
    private var isUsingData = false

    override fun startListening() {
        if (!doesSensorExist) {
            return
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                this
            )
            isListening = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun startUsing() {
        isUsingData = true
    }

    fun stopUsing() {
        isUsingData = false
    }

    override fun stopListening() {
        locationManager.removeUpdates(this)
        isListening = false
    }

    // Method to get the last known location
    fun getLastKnownLocation(): Location? {
        return try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }

    override fun onLocationChanged(location: Location) {

        if (!isUsingData) return  // Only process data when required

        val currentTimeMillis = System.currentTimeMillis()
        val nanoTime = System.nanoTime()
        val timestamp = formatTimestamp(currentTimeMillis, nanoTime)
        val data =
            "GPS,${location.latitude},${location.longitude},${location.altitude},${timestamp}"

        sensorDataState = "GPS: %.3f, %.3f, %.3f".format(
            location.latitude,
            location.longitude,
            location.altitude
        )

        sensorData.add(data)
        onSensorValueChanged?.invoke(
            listOf(
                location.latitude.toFloat(),
                location.longitude.toFloat(),
                location.altitude.toFloat(),
            )
        )
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    // Fallback handling if GPS data is not available
    override fun onProviderDisabled(provider: String) {
        sensorDataState = "GPS not available"
    }

    private fun formatTimestamp(timeMillis: Long, nanoAdjustment: Long): String {
        val date = Date(timeMillis)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.getDefault())
        val formattedDate = format.format(date)

        // Replace the comma with a dot in the  timestamp
        val formattedTimestamp = formattedDate.replace(",", ".")

        return formattedTimestamp
    }

}

