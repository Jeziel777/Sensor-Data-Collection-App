package com.example.pav_analytics.util

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import com.example.pav_analytics.FileManager.MediaFile
import com.example.pav_analytics.FileManager.MediaFileStorage.deleteMediaFile
import com.example.pav_analytics.FileManager.MediaFileStorage.getMediaFiles
import com.example.pav_analytics.FileManager.MediaFileStorage.updateMediaFile
import com.example.pav_analytics.FileManager.PictureFile
import com.example.pav_analytics.FileManager.VideoFile
import com.example.pav_analytics.FileManager.createZipFolder
import com.example.pav_analytics.FileManager.deleteFileFromExternalStorage
import com.example.pav_analytics.FileManager.getFileInputStreamFromExternalStorage
import com.example.pav_analytics.FileManager.getMediaStorageDirectory
import com.example.pav_analytics.FileManager.getZIPStorageDirectory
import com.example.pav_analytics.FileManager.isImageFile
import com.example.pav_analytics.FileManager.listRelatedFiles
import com.example.pav_analytics.FileManager.saveFileToExternalStorage
import com.example.pav_analytics.FileManager.zipFiles
import com.example.pav_analytics.Notifications.showNotification
import com.example.pav_analytics.restApp.uploadFileWithProgress
import com.example.pav_analytics.restApp.uploadPhotoWithProgress
import com.example.pav_analytics.restApp.uploadVideoWithProgress
import com.example.pav_analytics.ui.CardItem

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.File

// New Enum class to represent the state of the file
enum class FileState {
    SENT,
    NOT_SENT
}

@Composable
fun PhoneFileCardList(uid: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var availableFiles by remember { mutableStateOf(listOf<String>()) }
    val progressMap = remember { mutableStateMapOf<String, Int>() }
    val fileStateMap = remember { mutableStateMapOf<String, FileState>() }

    LaunchedEffect(Unit) {
        val mediaFiles = getMediaFiles(context)
        availableFiles = mediaFiles.keys.toList()
        availableFiles.forEach { filePath ->
            val mediaFile = mediaFiles[filePath]
            if (mediaFile != null) {
                val fileState = mediaFile.getFileState()
                fileStateMap[filePath] = fileState
                progressMap[filePath] = if (fileState == FileState.SENT) 100 else 0
            } else {
                fileStateMap[filePath] = FileState.NOT_SENT
                progressMap[filePath] = 0
            }
        }
    }

    if (availableFiles.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(availableFiles) { filePath ->
                val progress = progressMap[filePath] ?: 0
                val fileState = fileStateMap[filePath] ?: FileState.NOT_SENT
                val mediaFile = getMediaFiles(context)[filePath]

                CardItem(
                    fileName = filePath,
                    progress = progress,
                    fileState = fileState,
                    isPictureFile = mediaFile is PictureFile,
                    visualDistress = (mediaFile as? PictureFile)?.getVisualStress(),
                    onDownloadClick = {
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                val inputStream = getFileInputStreamFromExternalStorage(context, filePath)
                                inputStream?.let {
                                    saveFileToExternalStorage(context, it, filePath)
                                }
                            }
                            if (result != null) {
                                showNotification(
                                    context,
                                    "Download Successful",
                                    "File $filePath downloaded successfully.",
                                    notificationId = 1
                                )
                            } else {
                                showNotification(
                                    context,
                                    "Download Failed",
                                    "Failed to download file $filePath.",
                                    notificationId = 1
                                )
                            }
                        }
                    },
                    onSendClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                if (mediaFile is PictureFile) {
                                    // For PictureFile, send the file directly
                                    val visualDistress = mediaFile.getVisualStress()
                                    val gpsLocation = mediaFile.getGpsLocation()
                                    uploadPhotoWithProgress(
                                        context,
                                        filePath, // Sending the image file directly
                                        "https://140.203.17.132:443/report-distress",
                                        uid,
                                        gpsLocation?:"",
                                        visualDistress?.name ?: ""
                                    ) { newProgress ->
                                        progressMap[filePath] = newProgress
                                        if (newProgress == 100) {
                                            val updatedMediaFile = PictureFile(
                                                filePath,
                                                FileState.SENT,
                                                visualDistress
                                            )
                                            updateMediaFile(context, filePath, updatedMediaFile)
                                            fileStateMap[filePath] = FileState.SENT
                                        }
                                    }
                                } else if (mediaFile is VideoFile) {
                                    // For VideoFile, zip related files before sending
                                    val filesToSend = listRelatedFiles(context, filePath)
                                    val zipFolderDir = getZIPStorageDirectory(context)
                                    val zipFolderName = createZipFolder(filePath)
                                    val zipFile = File(zipFolderDir, zipFolderName)
                                    val outputFile = zipFiles(filesToSend, zipFile)

                                    uploadVideoWithProgress(
                                        context,
                                        outputFile.absolutePath,
                                        "https://140.203.17.132:443/upload-compressed",
                                        uid
                                    ) { newProgress ->
                                        progressMap[filePath] = newProgress
                                        if (newProgress == 100) {
                                            val updatedMediaFile = VideoFile(filePath, FileState.SENT)
                                            updateMediaFile(context, filePath, updatedMediaFile)
                                            fileStateMap[filePath] = FileState.SENT
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        coroutineScope.launch {
                            val success = withContext(Dispatchers.IO) {
                                deleteFileFromExternalStorage(context, filePath)
                            }
                            if (success) {
                                deleteMediaFile(context, filePath)
                                availableFiles = getMediaFiles(context).keys.toList()
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Could not delete file",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    } else {
        Text(
            text = "No files available",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}