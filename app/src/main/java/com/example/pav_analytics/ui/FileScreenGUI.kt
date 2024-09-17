package com.example.pav_analytics.ui

import android.annotation.SuppressLint
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.pav_analytics.FileManager.FileState
import com.example.pav_analytics.FileManager.VisualDistress
import com.example.pav_analytics.ui.components.FileTitleCard
import com.example.pav_analytics.ui.theme.DarkGrayColor
import com.example.pav_analytics.ui.theme.DarkerGrayColor
import com.example.pav_analytics.ui.theme.bluePavAnalytics
import com.example.pav_analytics.ui.theme.grayDarkTheme
import com.example.pav_analytics.ui.theme.greenDarkTheme
import com.example.pav_analytics.ui.theme.greenPavAnalytics
import com.example.pav_analytics.util.PhoneFileCardList

import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun CardItem(
    fileName: String,
    progress: Int,
    fileState: FileState,
    isPictureFile: Boolean,
    visualDistress: VisualDistress?,
    thumbnail: ImageBitmap?, // Add this parameter to pass the thumbnail
    onDownloadClick: () -> Unit,
    onSendClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val backgroundColor = when (fileState) {
        FileState.SENT -> if (isDarkTheme) greenPavAnalytics else greenPavAnalytics
        FileState.NOT_SENT -> if (isDarkTheme) grayDarkTheme else Color.Gray
    }

    // Use Card composable with elevation for shadow effect
    Card(
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // You can adjust the elevation for a stronger shadow
        colors = CardDefaults.cardColors(backgroundColor), // Correct background color handling
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { } // Add click behavior if necessary
    ) {
        Column(
            modifier = Modifier.padding(8.dp) // Inner padding within the card
        ) {

            // Row to display the thumbnail on the left and filename on the right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically // Ensure vertical alignment
            ) {
                // Display the thumbnail if available
                thumbnail?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "Thumbnail for $fileName",
                        modifier = Modifier
                            .size(64.dp) // Adjust thumbnail size as needed
                            .padding(end = 8.dp) // Space between thumbnail and text
                    )
                }

                // Display the filename
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f) // Take the remaining space
                )
            }

            if (isPictureFile) {
                Text(
                    text = "Visual Distress: ${visualDistress ?: "None"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }

            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButtonWithText(
                    icon = Icons.Default.Download,
                    text = "Download",
                    onClick = onDownloadClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(3.dp)
                )
                IconButtonWithText(
                    icon = Icons.Default.Send,
                    text = "Send",
                    onClick = onSendClick,
                    enabled = fileState != FileState.SENT,
                    modifier = Modifier
                        .weight(1f)
                        .padding(3.dp)
                )
                IconButtonWithText(
                    icon = Icons.Default.Delete,
                    text = "Delete",
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(3.dp)
                )
            }
        }
    }
}

@Composable
fun IconButtonWithText(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),  // Customize the corner radius
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp)  // Adjust icon size as needed
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 10.sp  // Smaller text size
            )
        }
    }
}

@SuppressLint("LogNotTimber")
@Composable
fun FileScreen(uid: String, selectMediaLauncher: ActivityResultLauncher<Array<String>>) {

    // Define colors for light and dark themes
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Gray else Color.LightGray
    val textColor = if (isDarkTheme) Color.Gray else Color.White
    val statusBarColor = MaterialTheme.colorScheme.primary

    var mediaListKey by remember { mutableIntStateOf(0) } // State to force recomposition
    val systemUiController = rememberSystemUiController()


    // Set the status bar color
    LaunchedEffect(statusBarColor) {
        systemUiController.setStatusBarColor(color = statusBarColor)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Background color for the entire FileScreen
    ) {
        // Use the TitleCard composable at the very top without padding
        FileTitleCard(
            title = "Files",
            backgroundColor = statusBarColor,
            textColor = textColor,
            onMediaImported = {
                mediaListKey++ // Increment key to force recomposition
            },
            selectMediaLauncher = selectMediaLauncher // Pass the launcher
        )
        // Add the rest of the content with padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Add padding around the rest of the content
        ) {
            PhoneFileCardList(uid, key = mediaListKey)
        }
    }
}