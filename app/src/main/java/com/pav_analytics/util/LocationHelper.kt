package com.pav_analytics.util

import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

private lateinit var fusedLocationClient: FusedLocationProviderClient

private fun startLocationUpdates(context: Context) {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val latitude = location.latitude
                val longitude = location.longitude
                // Save or use the GPS coordinates
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}


