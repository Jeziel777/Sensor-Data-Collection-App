package com.example.pav_analytics.util

import android.annotation.SuppressLint
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.core.content.ContextCompat
import android.content.Context
import android.location.Location
import android.widget.Toast
import java.io.File
import android.util.Log
import android.os.Build
import androidx.annotation.RequiresApi

import com.example.pav_analytics.CSVWritter.saveSensorDataToCSV
import com.example.pav_analytics.FileManager.MediaFileStorage
import com.example.pav_analytics.FileManager.PictureFile
import com.example.pav_analytics.FileManager.VideoFile
import com.example.pav_analytics.FileManager.VisualDistress
import com.example.pav_analytics.FileManager.getMediaStorageDirectory
import com.example.pav_analytics.FileManager.saveBitmapToExternalStorage
import com.example.pav_analytics.sensors.startSensors
import com.example.pav_analytics.sensors.stopSensors
import com.google.android.gms.location.LocationServices

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private var recording: Recording? = null
var sensorData: MutableList<String> = mutableListOf()
private var isRecordingInProgress = false

/**
 * Takes a photo using the provided camera controller and saves it to internal storage
 * @param applicationContext The application context
 * @param controller The LifecycleCameraController used to take the photo
 * @param onPhotoTaken Callback function to handle the bitmap of the taken photo
 */
fun takePhoto(
    applicationContext: Context,
    controller: LifecycleCameraController,
    capturedFileNameState: (String) -> Unit, // Pass the captured file name back to the Composable
    onVisualDistressSelected: (VisualDistress) -> Unit
) {
    // Initialize FusedLocationProviderClient to get location
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

    // Default GPS location to an empty string
    var gpsLocation = ""

    // Request location updates
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            // Get the latitude and longitude
            gpsLocation = "${location.latitude},${location.longitude}"
        } else {
            Log.w("Location", "GPS location not available")
        }
    }.addOnFailureListener {
        Log.e("Location", "Error getting location", it)
    }.addOnCompleteListener {
        // Always proceed to take the picture, whether or not the GPS was obtained
        captureAndSavePhoto(applicationContext, controller, gpsLocation, capturedFileNameState, onVisualDistressSelected)
    }
}

private fun captureAndSavePhoto(
    applicationContext: Context,
    controller: LifecycleCameraController,
    gpsLocation: String,
    capturedFileNameState: (String) -> Unit,
    onVisualDistressSelected: (VisualDistress) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(applicationContext),
        object : OnImageCapturedCallback() {
            @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                try {
                    val bitmap = image.toBitmap()
                    val currentTimeOfPicture = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "PA_photo_${currentTimeOfPicture}.png"

                    // Save the photo to external storage
                    val filePath = saveBitmapToExternalStorage(applicationContext, bitmap, fileName)

                    if (filePath != null) {
                        // Update the state in PhoneScreen with the captured file name
                        capturedFileNameState(fileName)

                        // Create a PictureFile instance and set the GPS location
                        val pictureFile = PictureFile(fileName, FileState.NOT_SENT, gpsLocation = gpsLocation)
                        MediaFileStorage.addMediaFile(applicationContext, fileName, pictureFile)
                        //Toast.makeText(applicationContext, "Photo saved with GPS: $gpsLocation", Toast.LENGTH_LONG).show()

                        // Trigger the callback to select visual distress
                        onVisualDistressSelected.invoke(VisualDistress.OTHER)

                    } else {
                        Toast.makeText(applicationContext, "Failed to save photo", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("Camera", "Error processing captured photo", e)
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Could not take Photo: ", exception)
            }
        }
    )
}

/**
 * Records a video using the provided LifecycleCameraController. If a recording is already in progress,
 * it stops the current recording and then starts a new one. The function also saves the recorded video
 * to external storage and handles different recording events, such as success or failure.
 * @param applicationContext The application context used for accessing resources and showing toast messages.
 * @param controller The LifecycleCameraController that manages the camera operations.
 * @param onRecordingStatusChanged A callback function that is invoked whenever the recording status changes.
 */
@SuppressLint("MissingPermission", "Wakelock", "WakelockTimeout")
fun recordVideo(
    applicationContext: Context,
    controller: LifecycleCameraController,
    onRecordingStatusChanged: () -> Unit
) {
    val documentDir = getMediaStorageDirectory(applicationContext)

    // Format the current time for the video file name
    val currentTimeOfVideo = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    val fileName = "PA_Video_${currentTimeOfVideo}.mp4"
    val file = File(documentDir, fileName)
    val lock = Any()

    synchronized(lock) {
        if (isRecordingInProgress) {
            recording?.stop()
            recording = null
            stopSensors()
            isRecordingInProgress = false
            return
        }

        sensorData.clear()

        recording = controller.startRecording(
            FileOutputOptions.Builder(file).build(),
            AudioConfig.create(true),
            ContextCompat.getMainExecutor(applicationContext)
        ) { event ->
            when (event) {
                is VideoRecordEvent.Finalize -> {
                    synchronized(lock) {
                        try {
                            if (event.hasError()) {
                                Toast.makeText(applicationContext, "Data Capture failed", Toast.LENGTH_LONG).show()

                            } else {
                                val videoFile = VideoFile(file.name, FileState.NOT_SENT)
                                MediaFileStorage.addMediaFile(applicationContext, file.name, videoFile)
                                Toast.makeText(applicationContext, "Data Capture succeed", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("Camera", "Error finalizing video", e)
                        } finally {
                            stopSensors()
                            saveSensorDataToCSV(applicationContext, currentTimeOfVideo)
                            recording = null
                            isRecordingInProgress = false
                            onRecordingStatusChanged()
                        }
                    }
                }
            }
        }
        startSensors()
        isRecordingInProgress = true
        onRecordingStatusChanged()
    }
}