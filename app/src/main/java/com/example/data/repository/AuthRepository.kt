package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository(private val context: Context) {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val prefs = context.getSharedPreferences("gym_track_auth_prefs", Context.MODE_PRIVATE)

    private val _token = MutableStateFlow<String?>(prefs.getString("auth_token", null))
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Администратор Зала") ?: "Администратор Зала")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun isLoggedIn(): Boolean = _token.value != null

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun loginOnline(email: String, pass: String): Result<String> {
        if (!isOnline()) {
            return Result.failure(Exception("Для авторизации требуется подключение к интернету"))
        }

        return try {
            val authResult = firebaseAuth.signInWithEmailAndPasswordSuspend(email, pass)
            val user = authResult.user ?: throw Exception("Пользователь не найден")
            val name = user.displayName ?: if (email.contains("@")) email.substringBefore("@") else "Администратор"
            val token = user.uid

            setAuth(token, name)
            Result.success(name)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка входа: ${e.localizedMessage}"))
        }
    }

    private suspend fun FirebaseAuth.signInWithEmailAndPasswordSuspend(email: String, pass: String): AuthResult =
        suspendCancellableCoroutine { continuation ->
            signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    if (continuation.isActive) continuation.resume(result)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
        }

    fun setAuth(token: String, userName: String) {
        prefs.edit()
            .putString("auth_token", token)
            .putString("user_name", userName)
            .apply()
        _token.value = token
        _userName.value = userName
    }

    fun logout() {
        try {
            firebaseAuth.signOut()
        } catch (_: Exception) {}
        prefs.edit().remove("auth_token").apply()
        _token.value = null
    }
}
