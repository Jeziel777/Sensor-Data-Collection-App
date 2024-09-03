package com.example.pav_analytics.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

sealed class AllScreen(){
    object SignUpScreen: AllScreen()
    object LoginScreen: AllScreen()
    object TermsAndConditionsScreen: AllScreen()
    object RecoverPasswordScreen: AllScreen()
}

object AppRouter{
    var currentScreen: MutableState<AllScreen> = mutableStateOf(AllScreen.LoginScreen)

    fun navigateTo(destination: AllScreen){
        currentScreen.value = destination
    }
}