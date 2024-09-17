package com.example.pav_analytics

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color

import androidx.navigation.compose.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.pav_analytics.FileManager.FileState
import com.example.pav_analytics.FileManager.MediaFileStorage
import com.example.pav_analytics.FileManager.PictureFile
import com.example.pav_analytics.FileManager.VideoDevice
import com.example.pav_analytics.FileManager.VideoFile
import com.example.pav_analytics.FileManager.VisualDistress
import com.example.pav_analytics.FileManager.getMediaStorageDirectory
import com.example.pav_analytics.navigation.AllScreen
import com.example.pav_analytics.navigation.AppRouter
import com.example.pav_analytics.network.Bluetooth
import com.example.pav_analytics.network.Wifi

// Import function and classes
import com.example.pav_analytics.ui.theme.PavAnalyticsTheme
import com.example.pav_analytics.util.PermissionsManager
import com.example.pav_analytics.sensors.initializeSensors
import com.example.pav_analytics.session.AuthManager
import com.example.pav_analytics.ui.BottomNavigationBar
import com.example.pav_analytics.ui.LoginScreen
import com.example.pav_analytics.ui.NavigationGraph
import com.example.pav_analytics.ui.RecoverPasswordScreen
import com.example.pav_analytics.ui.Screen
import com.example.pav_analytics.ui.SignUpScreen
import com.example.pav_analytics.ui.TermsAndConditionsScreen
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.ktor.util.InternalAPI
import java.io.File

const val REQUEST_CODE_IMPORT_MEDIA = 1001

// Main Activity class, entry point for the application.
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!PermissionsManager.hasRequiredPermissions(this)) {
            PermissionsManager.requestPermissions(this)
        }

        val selectMediaLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { selectedUri ->
                val cursor = contentResolver.query(selectedUri, null, null, null, null)
                val fileName = cursor?.use {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    it.moveToFirst()
                    it.getString(nameIndex)
                }

                // Get the MIME type of the selected file
                val mimeType = contentResolver.getType(selectedUri)

                // Use the original file name
                val mediaFolder = getMediaStorageDirectory(this)
                val file = File(mediaFolder, fileName ?: "default_media_name")


                // Check if the file is a video
                if (mimeType?.startsWith("video/") == true) {
                    val videoFile = VideoFile(file.name, FileState.NOT_SENT, VideoDevice.GOPRO)
                    MediaFileStorage.addMediaFile(applicationContext, file.name, videoFile)
                }

                // Copy the content from the Uri to the file
                contentResolver.openInputStream(selectedUri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Toast.makeText(this, "Media saved to: ${file.path}", Toast.LENGTH_SHORT).show()
            }
        }

        enableEdgeToEdge() // Enables edge-to-edge display to use screen space efficiently.
        FirebaseApp.initializeApp(this) // Initialize Firebase
        val db = Firebase.firestore
        setContent {
            PavAnalyticsTheme {
                AppEntryPoint(this, db, selectMediaLauncher) // Sets the content of the activity to MyApp Composable.
            }
        }
    }
}

@Composable
fun AppEntryPoint(
    activity: ComponentActivity,
    firestore: FirebaseFirestore,
    selectMediaLauncher: ActivityResultLauncher<Array<String>>
) {
    val isLoggedIn = remember { mutableStateOf(false) }
    val authManager = AuthManager(firestore)
    val userUID = remember { mutableStateOf<String?>(null) }

    if (isLoggedIn.value) {
        MyApp(
            activity = activity,
            onLogout = { isLoggedIn.value = false },
            uid = userUID.value!!,
            selectMediaLauncher = selectMediaLauncher) // Pass the media launcher to MyApp
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Crossfade(targetState = AppRouter.currentScreen) { currentState ->
                when (currentState.value) {
                    is AllScreen.SignUpScreen -> {
                        SignUpScreen(authManager)
                    }
                    is AllScreen.TermsAndConditionsScreen -> {
                        TermsAndConditionsScreen()
                    }
                    is AllScreen.LoginScreen -> {
                        LoginScreen(
                            auth = authManager,
                            onLoginSuccess = {
                                isLoggedIn.value = true
                                userUID.value = authManager.getCurrentUser()?.uid
                            })
                    }
                    is AllScreen.RecoverPasswordScreen -> {
                        RecoverPasswordScreen(authManager)
                    }
                }
            }
        }
    }
}

// Root Composable function for the app.
@Composable
fun MyApp (
    activity: ComponentActivity,
    onLogout: () -> Unit,
    uid: String,
    selectMediaLauncher: ActivityResultLauncher<Array<String>> // Pass this from activity
) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        initializeSensors(context)
    }
    val navController = rememberNavController() // Remember a NavController to handle navigation.
    Scaffold(
        modifier = Modifier.fillMaxSize(), // Modifier for full screen size.
        bottomBar = { BottomNavigationBar(navController) } // Bottom navigation bar component.
    ) { innerPadding ->
        NavigationGraph(navController, innerPadding, activity, onLogout, uid, selectMediaLauncher) // Sets up navigation for different screens.
    }
}

object DataStore {
    var connectedGoPro: String? = null
}

@InternalAPI
interface AppContainer {
    val applicationContext: Context
    val ble: Bluetooth
    val wifi: Wifi
}

@InternalAPI
@SuppressLint("NewApi")
class AppContainerImpl(override val applicationContext: Context) : AppContainer {
    override val ble = Bluetooth.getInstance(applicationContext)
    override val wifi = Wifi(applicationContext)
}