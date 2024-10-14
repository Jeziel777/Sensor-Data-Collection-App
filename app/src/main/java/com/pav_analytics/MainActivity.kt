package com.pav_analytics

// Import function and classes
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.pave_analytics.ui.BottomNavigationBar
import com.example.pave_analytics.ui.NavigationGraph
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.pav_analytics.FileManager.FileState
import com.pav_analytics.FileManager.MediaFileStorage
import com.pav_analytics.FileManager.VideoDevice
import com.pav_analytics.FileManager.VideoFile
import com.pav_analytics.FileManager.getMediaStorageDirectory
import com.pav_analytics.navigation.AllScreen
import com.pav_analytics.navigation.AppRouter
import com.pav_analytics.navigation.SystemBackButtonHandler
import com.pav_analytics.network.Bluetooth
import com.pav_analytics.network.Wifi
import com.pav_analytics.sensors.initializeSensors
import com.pav_analytics.session.AuthManager
import com.pav_analytics.ui.LoginScreen
import com.pav_analytics.ui.RecoverPasswordScreen
import com.pav_analytics.ui.SignUpScreen
import com.pav_analytics.ui.TermsAndConditionsScreen
import com.pav_analytics.ui.theme.PavAnalyticsTheme
import com.pav_analytics.util.PermissionsManager
import io.ktor.util.InternalAPI
import java.io.File

// Main Activity class, entry point for the application.
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!PermissionsManager.hasRequiredPermissions(this)) {
            PermissionsManager.requestPermissions(this)
        }

        val selectMediaLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
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
                AppEntryPoint(
                    this,
                    db,
                    selectMediaLauncher
                ) // Sets the content of the activity to MyApp Composable.
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
            selectMediaLauncher = selectMediaLauncher
        ) // Pass the media launcher to MyApp
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
fun MyApp(
    activity: ComponentActivity,
    onLogout: () -> Unit,
    uid: String,
    selectMediaLauncher: ActivityResultLauncher<Array<String>> // Pass this from activity
) {
    val context = LocalContext.current
    val navController = rememberNavController() // Remember a NavController to handle navigation.

    // Initialize sensors
    LaunchedEffect(context) {
        initializeSensors(context)
    }

    // SystemBackButtonHandler to manage back press
    SystemBackButtonHandler {
        if (!navController.popBackStack()) {
            activity.finish() // Default action if no more screens in the back stack
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), // Modifier for full screen size.
        bottomBar = { BottomNavigationBar(navController) } // Bottom navigation bar component.
    ) { innerPadding ->
        NavigationGraph(
            navController,
            innerPadding,
            activity,
            onLogout,
            uid,
            selectMediaLauncher
        ) // Sets up navigation for different screens.
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