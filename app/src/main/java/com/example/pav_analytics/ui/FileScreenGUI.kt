package com.example.pav_analytics.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Message
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
import com.example.pav_analytics.FileManager.VisualDistress

import com.example.pav_analytics.ui.components.TitleCard
import com.example.pav_analytics.ui.theme.grayDarkTheme
import com.example.pav_analytics.ui.theme.greenDarkTheme
import com.example.pav_analytics.util.FileState
import com.example.pav_analytics.util.PhoneFileCardList

import com.google.accompanist.systemuicontroller.rememberSystemUiController

// Modified CardItem function to take a FileState parameter and change the color of the card based on the state
@Composable
fun CardItem(
    fileName: String,
    progress: Int,
    fileState: FileState,
    onDownloadClick: () -> Unit,
    onSendClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onWriteComments:() -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Adjust background color based on fileState and theme
    val backgroundColor = when (fileState) {
        FileState.SENT ->
            if (isDarkTheme) greenDarkTheme
            else Color.Green  // Darker green in dark mode
        FileState.NOT_SENT ->
            if (isDarkTheme) grayDarkTheme
            else Color.Gray // Darker gray in dark mode
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(6.dp))
            .clickable { }
    ) {
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(8.dp)
        )
        LinearProgressIndicator(progress = progress / 100f, modifier = Modifier.padding(8.dp).fillMaxWidth())
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
                modifier = Modifier.weight(1f).padding(1.dp)
            )
            IconButtonWithText(
                icon = Icons.Default.Send,
                text = "Send",
                onClick = onSendClick,
                enabled = fileState != FileState.SENT,
                modifier = Modifier.weight(1f).padding(1.dp)
            )
            IconButtonWithText(
                icon = Icons.Default.Delete,
                text = "Delete",
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f).padding(1.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            IconButtonWithText(
                icon = Icons.Default.Message,
                text = "Write Comments",
                onClick = onWriteComments,
                modifier = Modifier.weight(1f).padding(2.dp)
            )
        }
    }
}

@Composable
fun CardItem(
    fileName: String,
    progress: Int,
    fileState: FileState,
    isPictureFile: Boolean,
    visualDistress: VisualDistress?,
    onDownloadClick: () -> Unit,
    onSendClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val backgroundColor = when (fileState) {
        FileState.SENT -> if (isDarkTheme) greenDarkTheme else Color.Green
        FileState.NOT_SENT -> if (isDarkTheme) grayDarkTheme else Color.Gray
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(6.dp))
            .clickable { }
    ) {
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(8.dp)
        )
        if (isPictureFile) {
            Text(
                text = "Visual Distress: ${visualDistress ?: "None"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
        LinearProgressIndicator(progress = progress / 100f, modifier = Modifier.padding(8.dp).fillMaxWidth())
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
                modifier = Modifier.weight(1f).padding(1.dp)
            )
            IconButtonWithText(
                icon = Icons.Default.Send,
                text = "Send",
                onClick = onSendClick,
                enabled = fileState != FileState.SENT,
                modifier = Modifier.weight(1f).padding(1.dp)
            )
            IconButtonWithText(
                icon = Icons.Default.Delete,
                text = "Delete",
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f).padding(1.dp)
            )
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
fun FileScreen(uid: String) {

    val isDarkTheme = isSystemInDarkTheme()
    // Define colors for light and dark themes
    val backgroundColor = if (isDarkTheme) Color.Gray else Color.LightGray

    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.primary

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
        TitleCard(
            title = "Files",
            backgroundColor = statusBarColor
        )
        // Add the rest of the content with padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Add padding around the rest of the content
        ) {
            PhoneFileCardList(uid)
        }
    }
}


