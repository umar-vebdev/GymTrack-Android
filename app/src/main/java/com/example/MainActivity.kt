package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.activity.SystemBarStyle
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.GymTrackTheme
import com.example.ui.viewmodel.GymViewModel

class MainActivity : ComponentActivity() {

    private val gymViewModel: GymViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkState by gymViewModel.isDarkMode.collectAsState()
            val systemIsDark = isSystemInDarkTheme()
            val useDarkTheme = isDarkState ?: systemIsDark

            DisposableEffect(useDarkTheme) {
                val style = if (useDarkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            GymTrackTheme(darkTheme = useDarkTheme) {
                AppNavigation(viewModel = gymViewModel)
            }
        }
    }
}
