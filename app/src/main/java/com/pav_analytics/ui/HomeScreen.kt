package com.example.pave_analytics.ui

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

import com.pav_analytics.Content.appInstructions
import com.pav_analytics.Content.goProSettings
import com.pav_analytics.Content.objectives
import com.pav_analytics.Content.phoneCameraInstructions
import com.pav_analytics.sensors.AccelerometerSensor
import com.pav_analytics.sensors.GPSSensor
import com.pav_analytics.sensors.GyroSensor
import com.pav_analytics.sensors.MagnetometerSensor
import com.pav_analytics.ui.components.HomeTitleCard
import com.pav_analytics.ui.theme.bluePavAnalytics
import com.pav_analytics.ui.theme.cyanBluePavAnalytics
import com.pav_analytics.ui.theme.greenPavAnalytics
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.pav_analytics.R

// HomeScreen menu
@Composable
fun HomeScreen(onLogout: () -> Unit, uid: String, navController: NavHostController) {

    // Define colors for light and dark themes
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Gray else Color.LightGray

    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current

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
        HomeTitleCard(
            title = "Pav-Analytics",
            backgroundColor = statusBarColor,
            onLogoutClick = {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                onLogout()
            },
        )
        // Add the new cards inside a Row
        InteractiveCardRow(navController)
        ExpandableCardList()
    }
}

@Composable
fun ExpandableCard(
    cardInfo: CardInfo,
    innerCardColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(color = cardInfo.backgroundColor, shape = RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = cardInfo.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal
            )
            Text(
                text = cardInfo.subtitle, // Display subtitle
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.7f) // Slightly faded color for subtitle
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                InnerCard(
                    title = cardInfo.innerCardTitle,
                    description = cardInfo.innerCardContent,
                    backgroundColor = innerCardColor,
                    imageResId = cardInfo.imageResId // Pass the image resource ID
                )
            }
        }
    }
}

data class CardInfo(
    val title: String,
    val subtitle: String,
    val innerCardTitle: String,
    val innerCardContent: String,
    val backgroundColor: Color,
    val imageResId: Int? = null // Optional image resource ID
)

@Composable
fun ExpandableCardList() {
    val cards = listOf(
        CardInfo(
            title = "Data collecting App",
            subtitle = "Data collection for active travel routes",
            innerCardTitle = "Getting started",
            innerCardContent = appInstructions,
            backgroundColor = bluePavAnalytics
        ),
        CardInfo(
            title = "About Us",
            subtitle = "An AI Based Pavement Condition Assessment Services",
            innerCardTitle = "Objectives",
            innerCardContent = objectives,
            backgroundColor = greenPavAnalytics
        ),
        CardInfo(
            title = "Recording with GoPro",
            subtitle = "How to set up your GoPro",
            innerCardTitle = "GoPro Settings",
            innerCardContent = goProSettings,
            backgroundColor = cyanBluePavAnalytics,
            imageResId = R.drawable.bicycle
        ),
        CardInfo(
            title = "Recording with Phone Camera",
            subtitle = "Tips for recording with your smartphone",
            innerCardTitle = "Phone Camera Tips",
            innerCardContent = phoneCameraInstructions,
            backgroundColor = bluePavAnalytics,
            imageResId = R.drawable.bicycle
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxHeight()
    ) {
        items(cards.size) { index ->
            val card = cards[index]
            ExpandableCard(
                cardInfo = card,
                innerCardColor = Color.Gray // Example color for the inner card
            )
        }
    }
}

@Composable
fun InnerCard(
    title: String,
    description: String,
    backgroundColor: Color,
    imageResId: Int? = null // Optional image resource ID
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Conditionally display the image if the resource ID is not null
            imageResId?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Adjust the height as needed
                )
            }
        }
    }
}

@Composable
fun RecordCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    InteractiveCard(
        title = "Go to Record",
        description = "Record video and\n" +
                "sensors readings\n",
        subtitle = "Available Sensors:",
        backgroundColor = Color.White,
        modifier = modifier,
        checkboxItems = readCheckboxValuesFromStorage(),
        onClick = onClick
    )
}

@Composable
fun GoProCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    InteractiveCard(
        title = "Connect GoPro",
        description = "Connect and download media files from GoPro",
        subtitle = "",
        backgroundColor = Color.White,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
fun InteractiveCard(
    title: String,
    description: String,
    subtitle: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    checkboxItems: List<CheckboxItem> = listOf(),
    onClick: () -> Unit = {} // Add an onClick parameter with a default empty lambda
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
            .clickable { onClick() } // Make the card clickable
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = bluePavAnalytics
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Iterate through the items and display each with a checkbox
            checkboxItems.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = null,  // Lock the checkbox (non-interactive)
                        enabled = false           // Disable the checkbox for user interaction
                    )
                    Text(
                        text = item.title,
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveCardRow(navController: NavHostController) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Make sure both cards match the height of the taller one
    ) {
        RecordCard(onClick = {
            // Navigate to PhoneScreen when RecordCard is clicked
            navController.navigate(Screen.Phone.route) {
                navController.graph.startDestinationRoute?.let { route ->
                    popUpTo(route) { saveState = true }
                }
                launchSingleTop = true
                restoreState = true
            }
        })
        Spacer(modifier = Modifier.width(8.dp))
        GoProCard(onClick = {
            // Navigate to GoProScreen when GoProCard is clicked
            navController.navigate(Screen.GoPro.route) {
                navController.graph.startDestinationRoute?.let { route ->
                    popUpTo(route) { saveState = true }
                }
                launchSingleTop = true
                restoreState = true
            }
        })
    }
}

data class CheckboxItem(
    val title: String,
    var isChecked: Boolean = false
)

// This is a placeholder function that simulates reading checkbox states from storage
@Composable
fun readCheckboxValuesFromStorage(): List<CheckboxItem> {
    val context = LocalContext.current // Get the current context
    val accelerometerSensor = remember { AccelerometerSensor(context) }
    val gyroSensor = remember { GyroSensor(context) }
    val gpsSensor = remember { GPSSensor(context) }
    val magnetometerSensor = remember { MagnetometerSensor(context) }

    return listOf(
        CheckboxItem("ACCL", isChecked = accelerometerSensor.doesSensorExist),
        CheckboxItem("GPS", isChecked = gpsSensor.doesSensorExist),
        CheckboxItem("GYRO", isChecked = gyroSensor.doesSensorExist),
        CheckboxItem("MAGT", isChecked = magnetometerSensor.doesSensorExist)
    )
}