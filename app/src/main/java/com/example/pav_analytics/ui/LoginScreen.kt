package com.example.pav_analytics.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.example.pav_analytics.ui.components.UnderlinedTextComponent
import com.google.firebase.firestore.FirebaseFirestore

// Usage in LoginScreen
@Composable
fun LoginScreen(auth: AuthManager, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // Define colors for light and dark themes
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val contentColor = if (isDarkTheme) Color.White else Color.Black

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(28.dp),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.height(20.dp))
            NormalTextComponent(value = "Hey, there", color = contentColor)
            HeadingTextComponent(value = "Welcome Back", color = contentColor)
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
            CustomTextFieldComponent(
                labelValue = "Password",
                iconImage = Icons.Default.Lock,
                isPasswordField = true,
                onValueChange = { password = it },
                textValue = password,
                textColor = contentColor,
                labelColor = contentColor,
                iconTint = contentColor
            )
            Spacer(modifier = Modifier.height(30.dp))
            UnderlinedTextComponent(value = "Forgot your Password?", color = contentColor, onClick = {
                AppRouter.navigateTo(AllScreen.RecoverPasswordScreen)
            })
            Spacer(modifier = Modifier.height(40.dp))
            ButtonComponent(value = "Login", onClick = {

                scope.launch {
                    emailLogin(email, password, context, auth, onLoginSuccess)
                }

            })
            Spacer(modifier = Modifier.height(20.dp))
            DividerTextComponent(color = contentColor)
            ClickableLoginTextComponent(tryingToLogin = false, textColor = contentColor, onTextSelected = {
                AppRouter.navigateTo(AllScreen.SignUpScreen)
            })
        }
    }
}

private suspend fun emailLogin(email: String, password: String, context: Context, auth: AuthManager, onLoginSuccess: () -> Unit) {
     if(email.isNotEmpty() && password.isNotEmpty()){
        when(val result = auth.signInWithEmailAndPassword(email, password)){
            is AuthRes.Success -> {
                onLoginSuccess()
            }
            is AuthRes.Error -> {
                Toast.makeText(context, "Failed to login, check email or Password", Toast.LENGTH_SHORT).show()
            }
        }
     }
     else{
         Toast.makeText(context, "Missing values", Toast.LENGTH_SHORT).show()
     }
}

@Preview
@Composable
fun LoginScreenPreview(){
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth =  AuthManager(firestore)
    LoginScreen( auth = auth, onLoginSuccess = { })
}