package com.pav_analytics.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.pav_analytics.session.AuthRes.Error
import kotlinx.coroutines.tasks.await

sealed class AuthRes<out T> {
    data class Success<T>(val data: T) : AuthRes<T>()
    data class Error(val errorMessage: String) : AuthRes<Nothing>()
}

class AuthManager(private val firestore: FirebaseFirestore) {

    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private var user = User("", "")

    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): AuthRes<FirebaseUser?> {
        return try {
            // Attempt to register with email and password
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            firebaseUser?.sendEmailVerification()?.await()  // Send email verification
            AuthRes.Success(firebaseUser)
        } catch (e: Exception) {
            Error(e.message ?: "Error during user creation and setup")
        }
    }

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): AuthRes<FirebaseUser?> {
        return try {
            // Attempt to sign in with email and password
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            // Check if email is verified
            if (firebaseUser?.isEmailVerified == true) {
                AuthRes.Success(firebaseUser)
            } else {
                // Sign out the user if email is not verified
                auth.signOut()
                Error("Email is not verified. Please verify your email before signing in.")
            }
        } catch (e: Exception) {
            // Return an error result with the exception message
            Error(e.message ?: "Error during sign-in")
        }
    }

    suspend fun resetPassword(email: String): AuthRes<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthRes.Success(Unit)
        } catch (e: Exception) {
            AuthRes.Error(e.message ?: "Error at password recovery")
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}