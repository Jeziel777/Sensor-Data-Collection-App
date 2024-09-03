package com.example.pav_analytics.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.ktor.util.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.pav_analytics.AppContainer
import com.example.pav_analytics.AppContainerImpl
import com.example.pav_analytics.FileManager.MediaFileStorage
import com.example.pav_analytics.FileManager.MediaFileStorage.loadMediaFiles
import com.example.pav_analytics.FileManager.PictureFile
import com.example.pav_analytics.FileManager.VideoFile
import com.example.pav_analytics.FileManager.isImageFile
import com.example.pav_analytics.GoPro.GoProMediaList
import com.example.pav_analytics.GoPro.ConnectBLE
import com.example.pav_analytics.GoPro.ConnectWiFi
import com.example.pav_analytics.ui.components.TitleCard
import com.example.pav_analytics.util.FileState

@OptIn(InternalAPI::class)
@Composable
fun GoProScreen(uid: String) {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val appContainer = remember { AppContainerImpl(context) }

    // Set the status bar color
    LaunchedEffect(statusBarColor) {
        systemUiController.setStatusBarColor(color = statusBarColor)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray) // Background color for the entire FileScreen
    ) {
        // Use the TitleCard composable at the very top without padding
        TitleCard(
            title = "GoPro Controls",
            backgroundColor = statusBarColor
        )
        // Add the rest of the content with padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Add padding around the rest of the content
        ) {
            GoProGUI(appContainer)
        }
    }
}

@OptIn(InternalAPI::class)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val context = LocalContext.current
    val appContainer = remember { AppContainerImpl(context) }
    GoProScreen("")
}

@OptIn(InternalAPI::class)
@SuppressLint("NewApi")
@Composable
fun GoProGUI(appContainer: AppContainer) {
    var isBleConnected by remember { mutableStateOf(false) }
    var isWifiConnected by remember { mutableStateOf(false) }
    var displayMessage by remember { mutableStateOf("") }
    var showMediaList by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        ConnectBLE().perform(appContainer)
                        isBleConnected = true
                    } catch (e: Exception) {
                        isBleConnected = false
                        displayMessage = "Failed to connect BLE: ${e.message}"
                    }
                }
            }) { Text("Connect BLE") }
            Icon(
                imageVector = if (isBleConnected) Icons.Filled.Bluetooth else Icons.Filled.BluetoothDisabled,
                contentDescription = "BLE Connection Status",
                tint = if (isBleConnected) Color.Green else Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                if (isBleConnected) {
                    coroutineScope.launch {
                        try {
                            ConnectWiFi().perform(appContainer)
                            isWifiConnected = true
                        } catch (e: Exception) {
                            isWifiConnected = false
                            displayMessage = "Failed to connect WiFi: ${e.message}"
                        }
                    }
                } else {
                    displayMessage = "BLE is not connected. Please connect BLE first."
                }
            }) { Text("Connect WiFi") }
            Icon(
                imageVector = if (isWifiConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                contentDescription = "WiFi Connection Status",
                tint = if (isWifiConnected) Color.Green else Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }

        Button(onClick = {
            if (isBleConnected && isWifiConnected) {
                showMediaList = true
                displayMessage = ""
            } else {
                showMediaList = false
                displayMessage = "WIFI or BLE is not connected"
            }
        }) {
            Text("Display Media")
        }

        if (showMediaList) {
            GoProMediaCardList(appContainer)
        } else if (displayMessage.isNotEmpty()) {
            Text(
                text = displayMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


@OptIn(InternalAPI::class)
@Composable
fun GoProMediaCardList(appContainer: AppContainer) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var goProMediaList by remember { mutableStateOf(listOf<String>()) }
    val mediaSavedInDevice = loadMediaFiles(context)
    var downloadingFile by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            goProMediaList = GoProMediaList().getGoProMediaList(appContainer) ?: listOf() // Fetch media files
        }
    }

    if (goProMediaList.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(goProMediaList) { filePath ->
                GoProCardItem(
                    fileName = filePath,
                    onDownloadClick = {
                        coroutineScope.launch {
                            downloadingFile = filePath
                            downloadProgress = 0f // reset progress
                            //val goProMediaFile = GoProMediaList().getFileByName(filePath, appContainer)
                            val goProMediaFile = GoProMediaList().getLargerFileByName(filePath, appContainer)
                            withContext(Dispatchers.Main) {
                                //if download
                                if (goProMediaFile != null && filePath !in mediaSavedInDevice.keys) {
                                    val mediaFile = if (isImageFile(filePath)) {
                                        PictureFile(filePath, FileState.NOT_SENT)
                                    } else {
                                        VideoFile(filePath, FileState.NOT_SENT)
                                    }
                                    MediaFileStorage.addMediaFile(context, filePath, mediaFile)
                                    Toast.makeText(
                                        context,
                                        "File downloaded successfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Could not download file",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        coroutineScope.launch {
                            val deleteResponse = GoProMediaList().deleteFileByName(filePath, appContainer)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, deleteResponse, Toast.LENGTH_LONG).show()
                                goProMediaList = GoProMediaList().getGoProMediaList(appContainer) ?: listOf()
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



// Modified CardItem function to take a FileState parameter and change the color of the card based on the state
@Composable
fun GoProCardItem(
    fileName: String,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    backgroundColor: Color = Color.Gray, // Set your default background color here){}
    isDownloading: Boolean = false,
    downloadProgress: Float? = null
    ) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(color = backgroundColor)
            .clickable { }
    ) {
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(8.dp)
        )
        if (isDownloading) {
            LinearProgressIndicator(progress = downloadProgress ?: 0f, modifier = Modifier.fillMaxWidth().padding(8.dp))
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDownloadClick,
                    modifier = Modifier.weight(1f).padding(4.dp)
                ) {
                    Text("Download")
                }
                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f).padding(4.dp)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}