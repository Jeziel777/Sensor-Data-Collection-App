package com.example.pav_analytics.FileManager

import android.content.Context
import java.io.File
import android.util.Log

import com.example.pav_analytics.util.FileState

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
        val baseString = "${mediaFile.getFileName()}$DELIMITER${mediaFile.getFileState()}"
        return if (mediaFile is PictureFile) {
            "$baseString$DELIMITER${mediaFile.getVisualStress() ?: ""}"
        } else {
            baseString
        }
    }

    private fun stringToMediaFile(data: String): MediaFile? {
        val parts = data.split(DELIMITER)
        return if (parts.size >= 2) {
            val fileName = parts[0]
            val fileState = FileState.valueOf(parts[1])
            return when {
                isPictureFile(fileName) -> {
                    val visualDistress = if (parts.size > 2) VisualDistress.valueOf(parts[2]) else null
                    PictureFile(fileName, fileState, visualDistress)
                }
                fileName.endsWith(".mp4", ignoreCase = true) -> VideoFile(fileName, fileState)
                else -> null
            }
        } else {
            null
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

    private fun isPictureFile(fileName: String): Boolean {
        val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        val fileExtension = fileName.substringAfterLast('.', "").toLowerCase()
        return imageExtensions.contains(fileExtension)
    }

}