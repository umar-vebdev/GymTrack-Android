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
    val serverUrl by viewModel.serverUrl.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()

    var customUrl by remember(serverUrl) { mutableStateOf(serverUrl) }
    var demoToggle by remember(isDemoMode) { mutableStateOf(isDemoMode) }

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

            // Server API URL Config
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ПОДКЛЮЧЕНИЕ К СЕРВЕРУ GYMTRACK API",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customUrl,
                            onValueChange = { customUrl = it },
                            label = { Text("Base API URL") },
                            placeholder = { Text("https://mygymserver.com/api/") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GymBgLight,
                                unfocusedContainerColor = GymBgLight,
                                focusedBorderColor = GymPrimaryIndigo
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Демо-режим (Автономная работа)", style = MaterialTheme.typography.bodyMedium, color = GymTextPrimary, fontWeight = FontWeight.Bold)
                                Text("Локальная баз данных Room с клиентами", style = MaterialTheme.typography.bodySmall, color = GymTextSecondary)
                            }
                            Switch(
                                checked = demoToggle,
                                onCheckedChange = { demoToggle = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GymPrimaryIndigo
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.updateServerSettings(customUrl, demoToggle) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Сохранить конфигурацию", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Data Management Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "УПРАВЛЕНИЕ ДАННЫМИ",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.resetSampleData() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GymPrimaryIndigo)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Пересоздать тестовые данные (Перезапуск DB)")
                        }
                    }
                }
            }
        }
    }
}
