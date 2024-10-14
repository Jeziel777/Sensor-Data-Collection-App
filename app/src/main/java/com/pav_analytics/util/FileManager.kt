package com.pav_analytics.util


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.pav_analytics.FileManager.FileState
import com.pav_analytics.FileManager.MediaFile
import com.pav_analytics.FileManager.MediaFileStorage.deleteMediaFile
import com.pav_analytics.FileManager.MediaFileStorage.getMediaFiles
import com.pav_analytics.FileManager.MediaFileStorage.updateMediaFile
import com.pav_analytics.FileManager.PictureFile
import com.pav_analytics.FileManager.VideoDevice
import com.pav_analytics.FileManager.VideoFile
import com.pav_analytics.FileManager.createZipFolderName
import com.pav_analytics.FileManager.deleteFileFromExternalStorage
import com.pav_analytics.FileManager.getFileInputStreamFromExternalStorage
import com.pav_analytics.FileManager.getMediaStorageDirectory
import com.pav_analytics.FileManager.getZIPStorageDirectory
import com.pav_analytics.FileManager.listRelatedFiles
import com.pav_analytics.FileManager.saveFileToExternalStorage
import com.pav_analytics.FileManager.zipFiles
import com.pav_analytics.Notifications.showNotification
import com.pav_analytics.restApp.uploadGoProVideoWithProgress
import com.pav_analytics.restApp.uploadPhotoWithProgress
import com.pav_analytics.restApp.uploadVideoWithProgress
import com.pav_analytics.ui.CardItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


// Function to resize bitmap to a fixed size
fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of the image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

@Composable
fun PhoneFileCardList(uid: String, key: Int) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var availableFiles by remember { mutableStateOf(listOf<String>()) }
    val progressMap = remember { mutableStateMapOf<String, Int>() }
    val fileStateMap = remember { mutableStateMapOf<String, FileState>() }
    val thumbnailMap = remember { mutableStateMapOf<String, ImageBitmap?>() }
    val mediaDirectory = getMediaStorageDirectory(context)

    // State to track the refresh status
    val isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadFiles(
            context,
            thumbnailMap,
            { newFiles -> availableFiles = newFiles },
            fileStateMap,
            progressMap,
            mediaDirectory
        )
    }

    SwipeRefresh(
        state = SwipeRefreshState(isRefreshing),
        onRefresh = {
            // Load new files when the user pulls down to refresh
            coroutineScope.launch {
                loadFiles(
                    context,
                    thumbnailMap,
                    { newFiles -> availableFiles = newFiles },
                    fileStateMap,
                    progressMap,
                    mediaDirectory
                )
            }
        }
    ) {
        if (availableFiles.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(availableFiles) { filePath ->
                    val progress = progressMap[filePath] ?: 0
                    val fileState = fileStateMap[filePath] ?: FileState.NOT_SENT
                    val mediaFile = getMediaFiles(context)[filePath]
                    val thumbnail = thumbnailMap[filePath]

                    CardItem(
                        fileName = filePath,
                        progress = progress,
                        fileState = fileState,
                        isPictureFile = mediaFile is PictureFile,
                        visualDistress = (mediaFile as? PictureFile)?.getVisualStress(),
                        thumbnail = thumbnail,
                        onDownloadClick = {
                            onDownloadClick(context, coroutineScope, filePath)
                        },
                        onSendClick = {
                            onSendClick(
                                context,
                                coroutineScope,
                                filePath,
                                mediaFile,
                                progressMap,
                                fileStateMap,
                                uid
                            )
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
}

// Function to load files
suspend fun loadFiles(
    context: Context,
    thumbnailMap: MutableMap<String, ImageBitmap?>,
    setAvailableFiles: (List<String>) -> Unit,
    fileStateMap: MutableMap<String, FileState>,
    progressMap: MutableMap<String, Int>,
    mediaDirectory: File
) {
    val mediaFiles = getMediaFiles(context)
    val newAvailableFiles = mediaFiles.keys.toList()

    // Update availableFiles using the setter
    setAvailableFiles(newAvailableFiles)

    newAvailableFiles.forEach { filePath ->
        val mediaFile = mediaFiles[filePath]
        if (mediaFile != null) {
            val fileState = mediaFile.getFileState()
            fileStateMap[filePath] = fileState
            progressMap[filePath] = if (fileState == FileState.SENT) 100 else 0

            val file = File(mediaDirectory, filePath)
            val thumbnail = generateThumbnail(mediaFile, file)
            thumbnailMap[filePath] = thumbnail
        } else {
            fileStateMap[filePath] = FileState.NOT_SENT
            progressMap[filePath] = 0
        }
    }
}

fun generateThumbnail(
    mediaFile: MediaFile,
    file: File,
    rotationDegrees: Float = 90f
): ImageBitmap? {
    val thumbnailWidth = 200
    val thumbnailHeight = 200

    return when (mediaFile) {
        is PictureFile -> {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(file.absolutePath, this)
                inSampleSize = calculateInSampleSize(this, thumbnailWidth, thumbnailHeight)
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            bitmap?.let {
                val rotatedBitmap = rotateBitmap(it, rotationDegrees)
                resizeBitmap(rotatedBitmap, thumbnailWidth, thumbnailHeight).asImageBitmap()
            }
        }

        is VideoFile -> {
            val thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(
                file.absolutePath,
                MediaStore.Video.Thumbnails.MINI_KIND
            )
            thumbnailBitmap?.let {
                val rotatedBitmap = rotateBitmap(it, 0f)
                resizeBitmap(rotatedBitmap, thumbnailWidth, thumbnailHeight).asImageBitmap()
            }
        }

        else -> null
    }
}

fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

// Handle Download
fun onDownloadClick(
    context: Context,
    coroutineScope: CoroutineScope,
    filePath: String
) {
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
}

// Handle Send
fun onSendClick(
    context: Context,
    coroutineScope: CoroutineScope,
    filePath: String,
    mediaFile: MediaFile?,
    progressMap: MutableMap<String, Int>,
    fileStateMap: MutableMap<String, FileState>,
    uid: String
) {
    coroutineScope.launch {
        withContext(Dispatchers.IO) {
            if (mediaFile is PictureFile) {
                val visualDistress = mediaFile.getVisualStress()
                val gpsLocation = mediaFile.getGpsLocation()
                uploadPhotoWithProgress(
                    context,
                    filePath,
                    "https://140.203.17.132:443/report-distress",
                    uid,
                    gpsLocation ?: "",
                    visualDistress?.name ?: ""
                ) { newProgress ->
                    progressMap[filePath] = newProgress
                    if (newProgress == 100) {
                        val updatedMediaFile =
                            PictureFile(filePath, FileState.SENT, visualDistress, gpsLocation)
                        updateMediaFile(context, filePath, updatedMediaFile)
                        fileStateMap[filePath] = FileState.SENT
                    }
                }
            } else if (mediaFile is VideoFile) {
                val videoDevice = mediaFile.getVideoDevice()
                val zipFolderDir = getZIPStorageDirectory(context)
                val filesToSend = listRelatedFiles(context, filePath)
                val outputFile: File
                val zipFile: File
                var url = ""

                if (videoDevice == VideoDevice.MOBILE) {
                    url = "https://140.203.17.132:443/upload-compressed"
                    val zipFolderName = createZipFolderName(filePath)
                    zipFile = File(zipFolderDir, zipFolderName)
                    outputFile = zipFiles(filesToSend, zipFile)

                    uploadVideoWithProgress(
                        context,
                        outputFile.absolutePath,
                        url,
                        videoDevice?.name ?: "",
                        uid
                    ) { newProgress ->
                        progressMap[filePath] = newProgress
                        if (newProgress == 100) {
                            val updatedMediaFile = VideoFile(filePath, FileState.SENT, VideoDevice.MOBILE)
                            updateMediaFile(context, filePath, updatedMediaFile)
                            fileStateMap[filePath] = FileState.SENT
                        }
                    }

                } else {
                    url = "https://140.203.17.132:443/demo_endpoint"
                    zipFile = File(zipFolderDir, filePath)

                    uploadGoProVideoWithProgress(context, filePath, url, uid) { newProgress ->
                        progressMap[filePath] = newProgress
                        if (newProgress == 100) {
                            val updatedMediaFile = VideoFile(filePath, FileState.SENT, VideoDevice.GOPRO)
                            updateMediaFile(context, filePath, updatedMediaFile)
                            fileStateMap[filePath] = FileState.SENT
                        }
                    }
                }
            }
        }
    }
}