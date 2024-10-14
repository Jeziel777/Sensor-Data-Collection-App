package com.example.pave_analytics.ui

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pav_analytics.R

import com.pav_analytics.ui.FileScreen

// Composable function for the bottom navigation bar.
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Phone,
        Screen.GoPro,
        Screen.File
    )
    val defaultIconColor = Color.Gray // Color for non-selected icons
    val activeIconColor = Color.Blue  // Color for the selected icon

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                icon = {
                    if (screen.icon is ImageVector) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (isSelected) activeIconColor else defaultIconColor // Apply color based on selection
                        )
                    } else if (screen.icon is Int) {
                        Image(
                            painter = painterResource(id = screen.icon),
                            contentDescription = screen.title,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                if (isSelected) activeIconColor else defaultIconColor
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = { Text(screen.title) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// Navigation graph that defines all navigation routes in the app.
@Composable
fun NavigationGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    activity: ComponentActivity,
    onLogout: () -> Unit,
    uid: String,
    selectMediaLauncher: ActivityResultLauncher<Array<String>>
) {
    NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
        composable(Screen.Home.route) { HomeScreen(onLogout, uid, navController) }
        composable(Screen.Phone.route) { PhoneScreen(activity = activity, uid) }
        composable(Screen.GoPro.route) { GoProScreen(uid) }
        composable(Screen.File.route) { FileScreen(uid, selectMediaLauncher) }
    }
}


// Definition of screens in the app as sealed class for type safety and encapsulation.
sealed class Screen(val route: String, val icon: Any, val title: String) {
    object Home : Screen("home", Icons.Filled.Home, "Home")
    object Phone : Screen("phone", Icons.Filled.PhotoCamera, "Camera")
    object GoPro : Screen("gopro", R.drawable.gopro, "GoPro")
    object File : Screen("file", Icons.Filled.Folder, "Files")
}