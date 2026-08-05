package com.example.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(context: Context) {

    private val prefs = context.getSharedPreferences("gym_track_auth_prefs", Context.MODE_PRIVATE)

    private val _token = MutableStateFlow<String?>(prefs.getString("auth_token", "demo-staff-token-123"))
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Администратор Зала") ?: "Администратор Зала")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _serverUrl = MutableStateFlow(prefs.getString("server_url", "https://gymtrack.example.com/api/") ?: "https://gymtrack.example.com/api/")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _isDemoMode = MutableStateFlow(prefs.getBoolean("is_demo_mode", true))
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    fun isLoggedIn(): Boolean = _token.value != null

    fun setAuth(token: String, userName: String) {
        prefs.edit()
            .putString("auth_token", token)
            .putString("user_name", userName)
            .apply()
        _token.value = token
        _userName.value = userName
    }

    fun logout() {
        prefs.edit().remove("auth_token").apply()
        _token.value = null
    }

    fun updateServerConfig(url: String, isDemo: Boolean) {
        val formattedUrl = if (url.endsWith("/")) url else "$url/"
        prefs.edit()
            .putString("server_url", formattedUrl)
            .putBoolean("is_demo_mode", isDemo)
            .apply()
        _serverUrl.value = formattedUrl
        _isDemoMode.value = isDemo
    }
}
