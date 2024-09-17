package com.example.pav_analytics.FileManager

import android.content.Context
import java.io.File
import android.util.Log

object MediaFileStorage {
    private val mediaFiles = mutableMapOf<String, MediaFile>()
    private const val FILE_NAME = "media_files.txt"
    private const val DELIMITER = ","

    private fun getFile(context: Context): File {
        val externalDir = getExternalStorageDirectory(context)
        val file = File(externalDir, FILE_NAME)
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: Exception) {
                Log.e("MediaFileStorage", "Error creating file: $FILE_NAME", e)
            }
        }
        return file
    }

    private fun mediaFileToString(mediaFile: MediaFile): String {
        return when (mediaFile) {
            is PictureFile -> {
                val visualDistress = mediaFile.getVisualStress()?.name ?: ""
                val gpsLocation = mediaFile.getGpsLocation() ?: ""
                "PictureFile|${mediaFile.getFileName()}|${mediaFile.getFileState()}|$visualDistress|$gpsLocation"
            }
            is VideoFile -> {
                "VideoFile|${mediaFile.getFileName()}|${mediaFile.getFileState()}|${mediaFile.getVideoDevice()}"
            }
            else -> ""
        }
    }


    private fun stringToMediaFile(data: String): MediaFile? {
        val parts = data.split("|")
        return when (parts[0]) {
            "PictureFile" -> {
                val fileName = parts[1]
                val fileState = FileState.valueOf(parts[2])
                val visualDistress = if (parts[3].isNotEmpty()) VisualDistress.valueOf(parts[3]) else null
                val gpsLocation = parts[4]
                PictureFile(fileName, fileState, visualDistress, gpsLocation)
            }
            "VideoFile" -> {
                val fileName = parts[1]
                val fileState = FileState.valueOf(parts[2])
                val videoDevice = VideoDevice.valueOf(parts[3])
                VideoFile(fileName, fileState, videoDevice)
            }
            else -> null
        }
    }


    fun saveMediaFiles(context: Context) {
        try {
            val file = getFile(context)
            file.printWriter().use { out ->
                mediaFiles.values.forEach { mediaFile ->
                    out.println(mediaFileToString(mediaFile))
                }
            }
        } catch (e: Exception) {
            Log.e("MediaFileStorage", "Error saving media files", e)
        }
    }

    fun loadMediaFiles(context: Context): MutableMap<String, MediaFile> {
        return try {
            val file = getFile(context)
            val mediaFilesMap = mutableMapOf<String, MediaFile>()
            file.forEachLine { line ->
                val mediaFile = stringToMediaFile(line)
                if (mediaFile != null) {
                    mediaFilesMap[mediaFile.getFileName()] = mediaFile
                }
            }
            mediaFilesMap
        } catch (e: Exception) {
            Log.e("MediaFileStorage", "Error loading media files", e)
            mutableMapOf()
        }
    }

    fun getMediaFiles(context: Context): Map<String, MediaFile> {
        val loadedMediaFiles = loadMediaFiles(context)
        mediaFiles.clear()
        mediaFiles.putAll(loadedMediaFiles)
        return mediaFiles.toMap()
    }

    fun addMediaFile(context: Context, key: String, mediaFile: MediaFile) {
        mediaFiles[key] = mediaFile
        saveMediaFiles(context)
    }

    fun deleteMediaFile(context: Context, key: String) {
        mediaFiles.remove(key)
        saveMediaFiles(context)
    }

    fun updateMediaFile(context: Context, key: String, mediaFile: MediaFile) {
        mediaFiles.put(key, mediaFile)
        saveMediaFiles(context)
    }

    fun isPictureFile(fileName: String): Boolean {
        val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        val fileExtension = fileName.substringAfterLast('.', "").toLowerCase()
        return imageExtensions.contains(fileExtension)
    }

}