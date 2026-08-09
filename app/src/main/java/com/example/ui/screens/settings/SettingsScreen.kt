package com.example.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GymPrimaryIndigo
import com.example.ui.viewmodel.GymViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GymViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val isDarkModeState by viewModel.isDarkMode.collectAsState()
    val systemIsDark = isSystemInDarkTheme()
    val currentlyDark = isDarkModeState ?: systemIsDark

    val currencies by viewModel.currencies.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var passwordVisible by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    // Show snackbar when backup status changes
    LaunchedEffect(backupStatus) {
        backupStatus?.let { status ->
            if (status != "Экспорт..." && status != "Импорт...") {
                scope.launch {
                    snackbarHostState.showSnackbar(status, duration = SnackbarDuration.Long)
                    viewModel.clearBackupStatus()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                CenterAlignedTopAppBar(
                    title = { Text("Настройки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Профиль администратора",
                style = MaterialTheme.typography.titleSmall,
                color = GymPrimaryIndigo,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Имя пользователя",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = userName.ifBlank { "Администратор" },
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Пароль",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            val passwordStr = "123456" 
                            Text(
                                text = if (passwordVisible) passwordStr else "•".repeat(passwordStr.length),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = if (passwordVisible) 0.sp else 4.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Показать пароль",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Оформление",
                style = MaterialTheme.typography.titleSmall,
                color = GymPrimaryIndigo,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (currentlyDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Тёмный режим",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = currentlyDark,
                        onCheckedChange = { viewModel.toggleTheme(systemIsDark) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Валюта",
                    style = MaterialTheme.typography.titleSmall,
                    color = GymPrimaryIndigo
                )
                TextButton(onClick = { showCurrencyDialog = true }, contentPadding = PaddingValues(0.dp)) {
                    Text("Добавить", style = MaterialTheme.typography.labelSmall)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column {
                    currencies.forEachIndexed { index, currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectCurrency(currency.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(currency.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                Text(currency.code, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (currency.isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Выбрано", tint = MaterialTheme.colorScheme.primary)
                            } else {
                                IconButton(onClick = { viewModel.deleteCurrency(currency) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        if (index < currencies.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Резервное копирование",
                style = MaterialTheme.typography.titleSmall,
                color = GymPrimaryIndigo,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            val isExporting = backupStatus == "Экспорт..."
            val isImporting = backupStatus == "Импорт..."

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column {
                    // Export Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isExporting && !isImporting) { viewModel.exportBackup() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isExporting) "Экспорт..." else "Экспортировать данные",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Сохранить .json файл в папку Загрузки",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    // Import Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isExporting && !isImporting) {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isImporting) "Импорт..." else "Импортировать данные",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Выбрать .json файл с устройства",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Опасная зона",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showResetDialog = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Сброс данных",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Удаление клиентов, визитов, абонементов и продаж",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    if (showCurrencyDialog) {
        AddCurrencyDialog(
            onDismiss = { showCurrencyDialog = false },
            onAdd = { name, code ->
                viewModel.addCurrency(name, code)
                showCurrencyDialog = false
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    "Сброс всех данных",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    "Это действие нельзя отменить.\n\nБудут удалены все клиенты, история визитов, покупки абонементов и все продажи.",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Удалить всё", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCurrencyDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить валюту", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название (напр. Рубль)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Код (напр. RUS)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, code) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo),
                enabled = name.isNotBlank() && code.isNotBlank()
            ) {
                Text("Сохранить", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
