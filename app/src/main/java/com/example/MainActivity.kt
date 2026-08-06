package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.GymTrackTheme
import com.example.ui.viewmodel.GymViewModel

class MainActivity : ComponentActivity() {

    private val gymViewModel: GymViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkState by gymViewModel.isDarkMode.collectAsState()
            val systemIsDark = isSystemInDarkTheme()
            val useDarkTheme = isDarkState ?: systemIsDark

            GymTrackTheme(darkTheme = useDarkTheme) {
                AppNavigation(viewModel = gymViewModel)
            }
        }
    }
}
