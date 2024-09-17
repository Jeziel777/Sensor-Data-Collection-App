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
import androidx.compose.material.icons.filled.*
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
import com.example.pav_analytics.navigation.AllScreen
import com.example.pav_analytics.navigation.AppRouter
import com.example.pav_analytics.session.AuthManager
import com.example.pav_analytics.session.AuthRes
import com.example.pav_analytics.ui.components.ButtonComponent
import com.example.pav_analytics.ui.components.CheckBoxComponent
import com.example.pav_analytics.ui.components.ClickableLoginTextComponent
import com.example.pav_analytics.ui.components.CustomTextFieldComponent
import com.example.pav_analytics.ui.components.DividerTextComponent
import com.example.pav_analytics.ui.components.HeadingTextComponent
import com.example.pav_analytics.ui.components.NormalTextComponent
import kotlinx.coroutines.launch

import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen( auth: AuthManager) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // Define colors for light and dark themes
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val contentColor = if (isDarkTheme) Color.White else Color.Black

    //var firstName by remember { mutableStateOf("") }
    //var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


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
            HeadingTextComponent(value = "Create an account", color = contentColor)
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
            CheckBoxComponent(
                value = "By continuing you accept our Privacy Policy and Terms of Use",
                onTextSelected = {AppRouter.navigateTo(AllScreen.TermsAndConditionsScreen)},
                textColor = contentColor)
            Spacer(modifier = Modifier.height(80.dp))
            ButtonComponent(value = "Register", onClick = {
                scope.launch {
                    signUp(email, password, auth, context)
                }
            })
            Spacer(modifier = Modifier.height(20.dp))
            DividerTextComponent(contentColor)
            ClickableLoginTextComponent(onTextSelected = {
                AppRouter.navigateTo(AllScreen.LoginScreen)
            }, textColor = contentColor)
        }
    }

}

private suspend fun signUp(
    email: String,
    password: String,
    auth: AuthManager,
    context: Context
) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        when(val result = auth.createUserWithEmailAndPassword(email, password)){
            is AuthRes.Success -> {
                Toast.makeText(context, "New user created, check email", Toast.LENGTH_SHORT).show()
                AppRouter.navigateTo(AllScreen.LoginScreen)
            }
            is AuthRes.Error -> {
                Toast.makeText(context, "Fail to register new user", Toast.LENGTH_SHORT).show()
            }
        }

    } else {
        Toast.makeText(context, "Missing values", Toast.LENGTH_SHORT).show()
    }
}

@Preview
@Composable
fun SignUpScreenPreview(){
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val authManager = AuthManager(firestore)
    SignUpScreen(authManager)
}