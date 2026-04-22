package com.closingassistant.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Login failed. Please try again.")
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(parseFirebaseError(e.message))
        }
    }

    suspend fun register(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Registration failed. Please try again.")
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(parseFirebaseError(e.message))
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun parseFirebaseError(message: String?): String {
        return when {
            message == null -> "An unexpected error occurred."
            message.contains("EMAIL_NOT_FOUND") || message.contains("INVALID_EMAIL") ->
                "Email address not found. Please check and try again."
            message.contains("WRONG_PASSWORD") || message.contains("invalid-credential") ->
                "Incorrect password. Please try again."
            message.contains("EMAIL_EXISTS") || message.contains("email-already-in-use") ->
                "This email is already registered. Please login instead."
            message.contains("WEAK_PASSWORD") || message.contains("weak-password") ->
                "Password is too weak. Please use at least 6 characters."
            message.contains("NETWORK_ERROR") || message.contains("network") ->
                "Network error. Please check your internet connection."
            message.contains("TOO_MANY_REQUESTS") ->
                "Too many attempts. Please try again later."
            else -> "Authentication failed. Please try again."
        }
    }
}
