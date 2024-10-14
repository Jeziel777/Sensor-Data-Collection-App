package com.pav_analytics.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object PermissionsManager {

    private const val PERMISSION_REQUEST_CODE = 1996

    private val APP_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    fun hasRequiredPermissions(context: Context): Boolean {
        return APP_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions(activity: ComponentActivity) {
        ActivityCompat.requestPermissions(activity, APP_PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

}