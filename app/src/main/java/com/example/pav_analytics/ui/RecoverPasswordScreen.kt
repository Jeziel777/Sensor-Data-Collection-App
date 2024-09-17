package com.example.pav_analytics.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pav_analytics.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

import com.example.pav_analytics.navigation.AllScreen
import com.example.pav_analytics.navigation.AppRouter
import com.example.pav_analytics.session.AuthManager
import com.example.pav_analytics.session.AuthRes
import com.example.pav_analytics.ui.components.ButtonComponent
import com.example.pav_analytics.ui.components.ClickableLoginTextComponent
import com.example.pav_analytics.ui.components.CustomTextFieldComponent
import com.example.pav_analytics.ui.components.DividerTextComponent
import com.example.pav_analytics.ui.components.HeadingTextComponent
import com.example.pav_analytics.ui.components.NormalTextComponent

@Composable
fun RecoverPasswordScreen(auth: AuthManager){

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // Define colors for light and dark themes
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val contentColor = if (isDarkTheme) Color.White else Color.Black

    var email by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(28.dp),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(id = R.drawable.logo_no_back),
                contentDescription = null, // You can provide a description here if needed
                modifier = Modifier
                    .size(200.dp) // Adjust size as needed
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(20.dp))
            NormalTextComponent(value = "Hey, there", color = contentColor)
            HeadingTextComponent(value = "Forgot Password?", color = contentColor)
            Spacer(modifier = Modifier.height(20.dp))
            CustomTextFieldComponent(
                labelValue = "Email",
                iconImage = Icons.Default.Email,
                isPasswordField = false,
                onValueChange = { email = it },
                textValue = email,
                textColor = contentColor,
                labelColor = contentColor,
                iconTint = contentColor
            )

            Spacer(modifier = Modifier.height(40.dp))
            ButtonComponent(value = "Reset Password", onClick = {

                scope.launch {
                    resetByEmailPassword(email, auth, context)
                }

            })
            Spacer(modifier = Modifier.height(20.dp))
            DividerTextComponent(contentColor)
            ClickableLoginTextComponent(tryingToLogin = false, onTextSelected = {
                AppRouter.navigateTo(AllScreen.SignUpScreen)
            }, textColor = contentColor)
        }
    }
}

private suspend fun resetByEmailPassword(email: String, auth: AuthManager, context: Context) {
    when(val result = auth.resetPassword(email)){
        is AuthRes.Success -> {
            Toast.makeText(context, "Email send", Toast.LENGTH_SHORT).show()
            AppRouter.navigateTo(AllScreen.LoginScreen)
        }
        is AuthRes.Error -> {
            Toast.makeText(context, "Error at sending recovery Password Email", Toast.LENGTH_SHORT).show()
        }
    }
}


@Preview
@Composable
fun RecoverPasswordScreenPreview(){
    RecoverPasswordScreen(auth = AuthManager(firestore = Firebase.firestore))
}

