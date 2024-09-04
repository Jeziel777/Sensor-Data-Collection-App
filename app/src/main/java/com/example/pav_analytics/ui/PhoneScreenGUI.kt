package com.example.pav_analytics.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.view.CameraController
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pav_analytics.FileManager.MediaFileStorage
import com.example.pav_analytics.FileManager.MediaFileStorage.getMediaFiles
import com.example.pav_analytics.FileManager.PictureFile
import com.example.pav_analytics.FileManager.VisualDistress

//import com.example.pav_analytics.sensors.orientationSensor
import com.example.pav_analytics.sensors.AndroidSensor
import com.example.pav_analytics.sensors.OrientationSensor
import com.example.pav_analytics.sensors.MeasurableSensor
import com.example.pav_analytics.sensors.accelerometerSensor
import com.example.pav_analytics.sensors.gpsSensor
import com.example.pav_analytics.sensors.gyroSensor
import com.example.pav_analytics.util.FileState
import com.example.pav_analytics.util.recordVideo
import com.example.pav_analytics.util.takePhoto

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker

//PhoneScreen menu
@Composable
fun PhoneScreen(activity: ComponentActivity, uid: String) {
    val context = LocalContext.current

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
        }
    }

    var isRecording by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var capturedFileName by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding()
    ) {
        CameraPreview(
            controller = controller,
            modifier = Modifier
                .fillMaxSize()
        )

        RecordingIndicator(
            isRecording = isRecording
        )

        DisplayCameraControl(
            controller = controller,
            context = context,
            isRecording = isRecording,
            onRecordClick = {
                recordVideo(
                    applicationContext = context,
                    controller = controller,
                    onRecordingStatusChanged = {
                        isRecording = !isRecording
                    }
                )
            },
            onTakePhotoClick = {
                takePhoto(
                    context,
                    controller = controller,
                    capturedFileNameState = { capturedFileName = it }, // Pass lambda to set capturedFileName
                    onVisualDistressSelected = { visualDistress ->
                        capturedFileName?.let { fileName ->
                            val pictureFile = PictureFile(fileName, FileState.NOT_SENT, visualDistress)
                            MediaFileStorage.addMediaFile(context, fileName, pictureFile)
                            Toast.makeText(context, "Photo saved", Toast.LENGTH_LONG).show()
                        }
                    }
                )
                showDialog = true
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showDialog) {
            VisualDistressDialog(
                onDismiss = { showDialog = false },
                onVisualDistressSelected = { selectedVisualDistress ->
                    capturedFileName?.let { fileName ->
                        val pictureFile = getMediaFiles(context)[fileName] as PictureFile
                        pictureFile.setVisualStress(selectedVisualDistress)
                        MediaFileStorage.addMediaFile(context, fileName, pictureFile)
                        Toast.makeText(context, "Photo saved with visual distress as: $selectedVisualDistress", Toast.LENGTH_LONG).show()
                    }
                    showDialog = false
                }
            )
        }
    }
}


@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
            this.controller = controller
            controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

@Composable
fun DisplayCameraControl(
    controller: LifecycleCameraController,
    context: Context,
    isRecording: Boolean,
    onRecordClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onTakePhotoClick
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take photo",
                        modifier = Modifier.size(48.dp)
                    )
                }
                Text(
                    text = "Take Photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onRecordClick
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = if (isRecording) "Pause Recording" else "Record Data",
                        tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Text(
                    text = if (isRecording) "Pause Recording" else "Record Data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RecordingIndicator(
    isRecording: Boolean
) {
    if (isRecording) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = "Recording",
                tint = Color.Red,
                modifier = Modifier
                    .size(48.dp)
                    .padding(top = 16.dp) // Optional padding to position the icon better
            )
            SensorInformation()
        }
    }
}

@OptIn(ObsoleteCoroutinesApi::class)
@Composable
fun SensorInformation() {
    // List of sensor data and their display states
    data class SensorData(
        val sensorType: MeasurableSensor,
        var displayData: MutableState<String>,
        val doesSensorExist: Boolean
    )

    val sensorList = listOf(
        SensorData(accelerometerSensor, remember { mutableStateOf("") }, accelerometerSensor.doesSensorExist),
        SensorData(gyroSensor, remember { mutableStateOf("") }, gyroSensor.doesSensorExist),
        SensorData(gpsSensor, remember { mutableStateOf("") }, gpsSensor.doesSensorExist),
        //SensorData(orientationSensor, remember { mutableStateOf("") }, orientationSensor.doesSensorExist)
    )

    LaunchedEffect(Unit) {
        // Ticker that ticks every second
        val tickerChannel = ticker(delayMillis = 1000, initialDelayMillis = 0)

        for (event in tickerChannel) {
            sensorList.forEach { sensor ->
                val newData = sensor.sensorType.sensorDataState
                // Update only if data has changed to avoid unnecessary recompositions
                if (sensor.displayData.value != newData) {
                    sensor.displayData.value = newData
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        sensorList.forEach { sensor ->
            if (sensor.doesSensorExist) {
                Text(
                    text = sensor.displayData.value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}


@Composable
fun VisualDistressDialog(
    onDismiss: () -> Unit,
    onVisualDistressSelected: (VisualDistress) -> Unit
) {
    val options = VisualDistress.entries.toTypedArray()
    var selectedOption by remember { mutableStateOf<VisualDistress?>(null) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Select Visual Distress") },
        text = {
            Column {
                options.forEach { distress ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedOption == distress,
                                onClick = { selectedOption = distress }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == distress,
                            onClick = { selectedOption = distress }
                        )
                        Text(text = distress.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedOption?.let {
                        onVisualDistressSelected(it)
                        onDismiss()
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
