package com.pav_analytics.ui

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
import androidx.compose.ui.unit.dp

import com.pav_analytics.R
import com.pav_analytics.navigation.AllScreen
import com.pav_analytics.navigation.AppRouter
import com.pav_analytics.session.AuthManager
import com.pav_analytics.session.AuthRes
import com.pav_analytics.ui.components.CheckBoxComponent
import com.pav_analytics.ui.components.ClickableLoginTextComponent
import com.pav_analytics.ui.components.CustomTextFieldComponent
import com.pav_analytics.ui.components.DividerTextComponent
import com.pav_analytics.ui.components.HeadingTextComponent
import com.pav_analytics.ui.components.NormalTextComponent
import com.pav_analytics.ui.components.RegisterButtonComponent
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(auth: AuthManager) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // Define colors for light and dark themes
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val contentColor = if (isDarkTheme) Color.White else Color.Black

    // Form state variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isCheckboxChecked by remember { mutableStateOf(false) }

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
            CustomTextFieldComponent(
                labelValue = "Confirm Password",
                iconImage = Icons.Default.Lock,
                isPasswordField = true,
                onValueChange = { confirmPassword = it },
                textValue = confirmPassword,
                textColor = contentColor,
                labelColor = contentColor,
                iconTint = contentColor
            )
            CheckBoxComponent(
                value = "By continuing you accept our Privacy Policy and Terms of Use",
                onCheckedChange = { isChecked -> isCheckboxChecked = isChecked },
                onTextSelected = { AppRouter.navigateTo(AllScreen.TermsAndConditionsScreen) },
                textColor = contentColor
            )
            Spacer(modifier = Modifier.height(40.dp))
            RegisterButtonComponent(
                value = "Register",
                enabled = isCheckboxChecked,
                onClick = {
                    scope.launch {
                        signUp(email, password, confirmPassword, auth, context)
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
    confirmPassword: String,
    auth: AuthManager,
    context: Context
) {
    if (email.isNotEmpty() && password.isNotEmpty() and confirmPassword.isNotEmpty()) {
        if (password == confirmPassword) {
            when (val resultx = auth.createUserWithEmailAndPassword(email, password)) {
                is AuthRes.Success -> {
                    Toast.makeText(context, "New user created, check email", Toast.LENGTH_SHORT)
                        .show()
                    AppRouter.navigateTo(AllScreen.LoginScreen)
                }

                is AuthRes.Error -> {
                    Toast.makeText(context, "Fail to register new user", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Password mismatch", Toast.LENGTH_SHORT).show()
        }

    } else {
        Toast.makeText(context, "Missing values", Toast.LENGTH_SHORT).show()
    }
}