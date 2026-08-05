package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel

@Composable
fun LoginScreen(viewModel: GymViewModel) {
    var email by remember { mutableStateOf("staff@gymtrack.com") }
    var password by remember { mutableStateOf("123456") }
    var showServerConfig by remember { mutableStateOf(false) }

    val currentServerUrl by viewModel.serverUrl.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()

    var customUrl by remember(currentServerUrl) { mutableStateOf(currentServerUrl) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBgLight),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 480.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GymPrimaryIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "GymTrack Logo",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "GymTrack",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = GymTextPrimary
                )

                Text(
                    text = "Caspian Fitness Club",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GymTextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Email / Phone Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email или телефон сотрудника") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GymTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = GymBgLight,
                        focusedContainerColor = GymSurfaceWhite,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = GymPrimaryIndigo
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GymTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = GymBgLight,
                        focusedContainerColor = GymSurfaceWhite,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = GymPrimaryIndigo
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Войти в систему",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { showServerConfig = !showServerConfig }) {
                    Icon(
                        imageVector = if (showServerConfig) Icons.Default.ExpandLess else Icons.Default.Settings,
                        contentDescription = null,
                        tint = GymPrimaryIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showServerConfig) "Скрыть настройки сервера" else "Настройки подключения к API",
                        color = GymPrimaryIndigo
                    )
                }

                AnimatedVisibility(visible = showServerConfig) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(GymBgLight, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "URL сервера API:",
                            style = MaterialTheme.typography.labelMedium,
                            color = GymTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customUrl,
                            onValueChange = { customUrl = it },
                            placeholder = { Text("https://mygym.com/api/") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = GymSurfaceWhite,
                                focusedContainerColor = GymSurfaceWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.updateServerSettings(customUrl, isDemoMode)
                                showServerConfig = false
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
                        ) {
                            Text("Сохранить URL", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
