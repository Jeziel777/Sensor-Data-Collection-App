package com.pav_analytics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pav_analytics.Content.TermsTextContent
import com.pav_analytics.navigation.AllScreen
import com.pav_analytics.navigation.AppRouter
import com.pav_analytics.navigation.SystemBackButtonHandler
import com.pav_analytics.ui.components.HeadingTextComponent


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
        // Wrap content in LazyColumn for scrolling
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                HeadingTextComponent(value = "Privacy Policy", color = contentColor)
            }
            item {
                TermsTextContent(color = contentColor)
            }
        }
        // Handle back button to navigate back
        SystemBackButtonHandler {
            AppRouter.navigateTo(AllScreen.SignUpScreen)
        }
    }
}

@Preview
@Composable
fun TermsAndConditionsScreenPreview() {
    TermsAndConditionsScreen()
}
