package com.example.pav_analytics.sensors

import android.annotation.SuppressLint
import android.content.Context
import java.io.File
import java.io.IOException

lateinit var accelerometerSensor: AccelerometerSensor
lateinit var gyroSensor: GyroSensor
lateinit var gpsSensor: GPSSensor
//lateinit var orientationSensor: OrientationSensor

fun initializeSensors(context: Context) {
    accelerometerSensor = AccelerometerSensor(context)
    gyroSensor = GyroSensor(context)
    gpsSensor = GPSSensor(context).apply {
        startListening() // Start GPS early to acquire lock
    }
    //orientationSensor = OrientationSensor(context)
}


@SuppressLint("NewApi")
fun startSensors() {
    accelerometerSensor.startListening(200)
    gyroSensor.startListening(100)
    gpsSensor.startUsing()  // Start using GPS data when recording starts
    //orientationSensor.startListening()
}

fun stopSensors() {
    accelerometerSensor.stopListening()
    gyroSensor.stopListening()
    gpsSensor.stopUsing()  // Stop using GPS data but keep the GPS sensor active
    //orientationSensor.stopListening()
}

/**
 * Embeds sensor data into a video file as subtitles using ffmpeg.
 * This function takes a video file and a corresponding timestamp to locate the sensor data
 * file (.srt format). It uses ffmpeg to add the subtitles from the sensor data file into
 * the video file and saves the resulting video with an updated filename that includes
 * the embedded sensor data.
 * @param videoFile The video file into which the sensor data will be embedded.
 * @param applicationContext The context of the application.
 * @param currentTimeOfVideo The timestamp used to locate the corresponding sensor data file.
 */
fun embedSensorDataIntoVideo(videoFile: File, applicationContext: Context, currentTimeOfVideo: String) {
    // Create a reference to the sensor data file using the same timestamp as the video file
    val sensorDataFile = File(videoFile.parentFile, "sensor_data_${currentTimeOfVideo}.srt")

    // Create a temporary output file to ensure data integrity
    val tempOutputFile = File(videoFile.parentFile, "temp_PavAnalytics_Video_with_Sensors_${currentTimeOfVideo}.mp4")

    try {
        // Prepare the ffmpeg command to embed the subtitles into the video
        val command = arrayOf(
            "ffmpeg",
            "-i", videoFile.absolutePath,  // Input video file
            "-vf", "subtitles=${sensorDataFile.absolutePath}",  // Video filter to add subtitles
            "-c:a", "copy",  // Copy the audio without re-encoding
            tempOutputFile.absolutePath  // Temporary output video file
        )

        // Start the process to execute the ffmpeg command
        val process = ProcessBuilder(*command)
            .redirectErrorStream(true)  // Redirect error stream to standard output
            .start()

        // Read and print the process output
        process.inputStream.bufferedReader().forEachLine { println(it) }
        // Wait for the process to complete
        val exitCode = process.waitFor()

        if (exitCode == 0) {
            // Replace the original video file with the temporary output file
            if (videoFile.delete()) {
                if (!tempOutputFile.renameTo(videoFile)) {
                    throw IOException("Failed to rename temp file to original file")
                }
            } else {
                throw IOException("Failed to delete original video file")
            }
        } else {
            throw IOException("ffmpeg process failed with exit code $exitCode")
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    } finally {
        // Clean up temporary file if it still exists
        if (tempOutputFile.exists()) {
            tempOutputFile.delete()
        }
    }
}