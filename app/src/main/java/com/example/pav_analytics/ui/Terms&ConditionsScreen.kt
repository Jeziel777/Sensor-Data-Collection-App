package com.example.pav_analytics.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pav_analytics.navigation.AllScreen
import com.example.pav_analytics.navigation.AppRouter
import com.example.pav_analytics.navigation.SystemBackButtonHandler
import com.example.pav_analytics.ui.components.HeadingTextComponent
import com.example.pav_analytics.ui.components.NormalTextComponent

@Composable
fun TermsAndConditionsScreen() {
    val isDarkTheme = isSystemInDarkTheme()

    // Define colors for light and dark themes
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val contentColor = if (isDarkTheme) Color.White else Color.Black

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(backgroundColor),
        color = backgroundColor
    ) {
        Column {
            HeadingTextComponent(value = "Terms and Conditions", color = contentColor)
            NormalTextComponent(value = "Our terms and conditions are ...", color = contentColor)
        }
        SystemBackButtonHandler{
            AppRouter.navigateTo(AllScreen.LoginScreen)
        }
    }
}

@Preview
@Composable
fun TermsAndConditionsScreenPreview(){
    TermsAndConditionsScreen()
}
