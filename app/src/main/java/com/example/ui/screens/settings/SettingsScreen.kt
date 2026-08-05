package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel

@Composable
fun SettingsScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(GymBgLight)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Настройки системы GymTrack",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = GymTextPrimary
                )
            }

            // User Staff Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = GymPrimaryIndigo,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(userName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GymTextPrimary)
                            Text("Роль: Кассир / Администратор зала", style = MaterialTheme.typography.bodySmall, color = GymTextSecondary)
                        }
                        Button(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.buttonColors(containerColor = GymRoseAlert),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Выйти", color = Color.White)
                        }
                    }
                }
            }

        }
    }
}
