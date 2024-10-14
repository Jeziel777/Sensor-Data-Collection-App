package com.pav_analytics.FileManager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Creates and returns a directory in external storage for the app to save files.
 * @param context The application context
 * @return The file representing the directory
 */
fun getExternalStorageDirectory(context: Context): File {
    // Using DIRECTORY_DOCUMENTS to create a more generic directory for different file types
    val externalDir =
        File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Pav-Analytics_Folder")
    if (!externalDir.exists()) {
        externalDir.mkdirs()
    }
    return externalDir
}

fun getCSVStorageDirectory(context: Context): File {
    val csvDirectory = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        "Pav-Analytics_Folder/CSV_files"
    )
    if (!csvDirectory.exists()) {
        csvDirectory.mkdirs()
    }
    return csvDirectory
}

fun getMediaStorageDirectory(context: Context): File {
    val mediaDirectory = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        "Pav-Analytics_Folder/Media"
    )
    if (!mediaDirectory.exists()) {
        mediaDirectory.mkdirs()
    }
    return mediaDirectory
}

fun getZIPStorageDirectory(context: Context): File {
    val zipDirectory = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        "Pav-Analytics_Folder/zip_files"
    )
    if (!zipDirectory.exists()) {
        zipDirectory.mkdirs()
    }
    return zipDirectory
}

/**
 * Saves a Bitmap to internal storage
 * @param context The application context
 * @param bitmap The bitmap to save
 * @param fileName The name of the file to save the bitmap as
 * @return The file path of the saved bitmap, or null if saving fails
 */
fun saveBitmapToExternalStorage(context: Context, bitmap: Bitmap, fileName: String): String? {
    val folder = getMediaStorageDirectory(context)
    val file = File(folder, fileName)
    return try {
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun saveVideoToExternalStorage(
    context: Context,
    videoInputStream: InputStream,
    fileName: String
): String? {
    val folder = getMediaStorageDirectory(context)
    val file = File(folder, fileName)

    return try {
        FileOutputStream(file).use { fos ->
            val fileChannel: FileChannel = fos.channel
            val inputChannel = Channels.newChannel(videoInputStream)
            fileChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE)
        }
        Log.i(
            "FileSave",
            "File saved successfully: ${file.absolutePath}, size: ${file.length()} bytes"
        )
        file.absolutePath
    } catch (e: IOException) {
        Log.e("FileSave", "Error saving file", e)
        null
    }
}

fun saveGoProFileToExternalStorage(context: Context, file: File): Boolean {
    // Get the media storage directory
    val folder = getMediaStorageDirectory(context)

    // Create the output file in the media storage directory
    val outputFile = File(folder, file.name)

    return try {
        file.inputStream().use { inputStream ->
            FileOutputStream(outputFile).use { fos ->
                val sizeInBytes = 40 * 1024 * 1024 // 40 MB in bytes
                val buffer = ByteArray(sizeInBytes) // 40 MB buffer
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    fos.write(buffer, 0, bytesRead)
                }
            }
        }
        true
    } catch (e: IOException) {
        false
    }
}


@SuppressLint("NewApi")
fun saveFileToExternalStorage(
    context: Context,
    inputStream: InputStream,
    fileName: String
): String? {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
    }

    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    return if (uri != null) {
        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                BufferedOutputStream(outputStream).use { bos ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        bos.write(buffer, 0, bytesRead)
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}


fun getFileInputStreamFromExternalStorage(context: Context, fileName: String): InputStream? {
    val folder = getMediaStorageDirectory(context)
    val file = File(folder, fileName)
    return try {
        FileInputStream(file)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}


fun saveSensorDataToFile(directory: File, sensorData: List<String>, currentTimeOfVideo: String) {
    val sensorDataFile = File(directory, "sensor_data_${currentTimeOfVideo}.srt")
    writeSensorDataToSrtFile(sensorData, sensorDataFile)
}

fun writeSensorDataToSrtFile(sensorData: List<String>, outputFile: File) {
    outputFile.printWriter().use { writer ->
        sensorData.forEachIndexed { index, data ->
            val timestamp = data.substringAfterLast(", ").trim()
            val timeStamp = timestamp.split(" ")[0]
            writer.println("${index + 1}")
            writer.println("$timeStamp --> ${nextTimestamp(timeStamp)}")
            writer.println(data)
            writer.println()
        }
    }
}

private fun nextTimestamp(timestamp: String): String {
    val format = SimpleDateFormat("HH:mm:ss,SSS", Locale.getDefault())
    val date = format.parse(timestamp) ?: return timestamp
    val nextDate = Date(date.time + 1000)  // Adding 1 second for simplicity
    return format.format(nextDate)
}

fun deleteFileFromExternalStorage(context: Context, fileName: String): Boolean {
    val mediaFolder = getMediaStorageDirectory(context)
    val file = File(mediaFolder, fileName)
    deleteRelatedCSVFiles(context, fileName)
    deleteRelatedZIPFiles(context, fileName)
    return file.delete()
}

fun deleteRelatedCSVFiles(context: Context, fileName: String) {
    val csvFolder = getCSVStorageDirectory(context)
    val currentTimeOfVideo = extractCurrentTimeOfVideo(fileName)

    if (currentTimeOfVideo != null) {
        val files = csvFolder.listFiles()

        files?.forEach { file ->
            if (file.name.contains(currentTimeOfVideo)) {
                file.delete()
            }
        }
    }
}

fun deleteRelatedZIPFiles(context: Context, fileName: String) {
    val zipFolder = getZIPStorageDirectory(context)
    val currentTimeOfVideo = extractCurrentTimeOfVideo(fileName)

    if (currentTimeOfVideo != null) {
        val files = zipFolder.listFiles()

        files?.forEach { file ->
            if (file.name.contains(currentTimeOfVideo)) {
                file.delete()
            }
        }
    }
}

fun extractCurrentTimeOfVideo(fileName: String): String? {
    val prefix = "Video_"
    val suffix = ".mp4"

    // Check if the filename starts with the expected prefix and ends with the expected suffix
    if (fileName.startsWith(prefix) && fileName.endsWith(suffix)) {
        // Extract the timestamp part from the filename
        val startIndex = prefix.length
        val endIndex = fileName.length - suffix.length
        return fileName.substring(startIndex, endIndex)
    }

    // Return null if the filename doesn't match the expected format
    return null
}


fun createZipFolderName(fileName: String): String {
    val currentTimeOfVideo = extractCurrentTimeOfVideo(fileName)
    val newZipFile = "zip_${currentTimeOfVideo}.zip"
    return newZipFile
}

fun listRelatedFiles(context: Context, fileName: String): List<File> {
    val relatedFiles = mutableListOf<File>() // String to store file list

    // add the video file to list
    val mediaFolder = getMediaStorageDirectory(context)
    val mp4File = File(mediaFolder, fileName)
    relatedFiles.add(mp4File)

    //get directory where CSV files are stored
    val csvFolder = getCSVStorageDirectory(context)
    val currentTimeOfVideo = extractCurrentTimeOfVideo(fileName)

    if (currentTimeOfVideo != null) {
        val files = csvFolder.listFiles()

        files?.forEach { file ->
            if (file.name.contains(currentTimeOfVideo)) {
                relatedFiles.add(file)
            }
        }
    }
    return relatedFiles
}

fun listFilesInExternalStorage(context: Context): List<String> {
    val folder = getMediaStorageDirectory(context)
    return folder.list()?.toList() ?: emptyList()
}

// Helper function to determine if a file is an image
fun isImageFile(fileName: String): Boolean {
    val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    val fileExtension = fileName.substringAfterLast('.', "").toLowerCase()
    return imageExtensions.contains(fileExtension)
}