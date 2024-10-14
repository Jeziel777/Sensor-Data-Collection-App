package com.pav_analytics.CSVWritter

import android.content.Context
import com.pav_analytics.FileManager.getCSVStorageDirectory
import com.pav_analytics.util.sensorData
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

fun saveSensorDataToCSV(context: Context, currentTimeOfVideo: String) {
    val dataMap = mutableMapOf(
        "ACL" to mutableListOf<String>(),
        "GRO" to mutableListOf<String>(),
        "GPS" to mutableListOf<String>(),
        "MAG" to mutableListOf<String>(),
        //"ORI" to mutableListOf<String>()
    )

    // Process the sensorData list
    for (data in sensorData) {
        val prefix = data.substring(0, 3)
        dataMap[prefix]?.add(data)
    }

    // Save the data to CSV files
    fun saveDataToCSV(fileName: String, data: List<String>, header: String? = null) {
        val folder = getCSVStorageDirectory(context)
        val file = File(folder, fileName)
        val writer = BufferedWriter(FileWriter(file))

        // Write the header line if provided
        header?.let {
            writer.write(it)
            writer.newLine()
        }

        for (line in data) {
            // Remove the prefix and the following comma before writing to the file
            val modifiedLine = line.substringAfter(',')
            writer.write(modifiedLine)
            writer.newLine()
        }

        writer.flush()
        writer.close()
    }

    // Determine headers based on the key (prefix)
    dataMap.forEach { (key, value) ->
        val header = when (key) {
            "ACL" -> "Y acceleration m/s^2,-X acceleration m/s^2,Z acceleration m/s^2 Z,Sample time [seg]"  // Accelerometer header
            "GRO" -> "Gyro -Y [rad/s],-X [rad/s],Z [rad/s],Sample time [seg]"                               // Gyro header
            "GPS" -> "GPS (Lat.) [deg],GPS (Long.) [deg],GPS (Alt.) [m],Sample time [seg]"                  // Header for GPS data
            "MAG" -> "Magnetic Field X [µT],Magnetic Field Y [µT],Magnetic Field Z [µT],Sample time [seg]"   // Magnetometer header
            //"ORI" -> "azimuth,pitch,roll,Time"
            else -> null                                // No header for unknown keys
        }

        saveDataToCSV("${key}_${currentTimeOfVideo}_data.csv", value, header)
    }
}
