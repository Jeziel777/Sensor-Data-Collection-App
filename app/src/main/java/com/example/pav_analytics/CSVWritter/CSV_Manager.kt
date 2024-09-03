package com.example.pav_analytics.CSVWritter

import android.content.Context
import com.example.pav_analytics.FileManager.getCSVStorageDirectory
import com.example.pav_analytics.util.sensorData

import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter

fun saveSensorDataToCSV(context: Context, currentTimeOfVideo: String) {
    val dataMap = mutableMapOf(
        "ACL" to mutableListOf<String>(),
        "GRO" to mutableListOf<String>(),
        "GPS" to mutableListOf<String>(),
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
            "ACL" -> "X m/s^2,Y m/s^2,Z m/s^2,Time"     // Example header for ACL data
            "GRO" -> "X rad/s,Y rad/s,Z rad/s,Time"     // Example header for GRO data
            "GPS" -> "Latitude,Longitude,Altitude,Time" // Example header for GPS data
            //"ORI" -> "azimuth,pitch,roll,Time"
            else -> null                                // No header for unknown keys
        }

        saveDataToCSV("${key}_${currentTimeOfVideo}_data.csv", value, header)
    }
}
