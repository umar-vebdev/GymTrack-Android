package com.example.ui.screens.tariffs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MembershipType
import com.example.ui.theme.*
import com.example.ui.components.GlobalSettingsButton
import com.example.ui.viewmodel.GymViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TariffsScreen(
    viewModel: GymViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tariffs by viewModel.membershipTypesAdmin.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val cCode = selectedCurrency?.code ?: "TJS"

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTariff by remember { mutableStateOf<MembershipType?>(null) }
    
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                CenterAlignedTopAppBar(
                    title = { Text("Управление тарифами", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                    actions = {
                        GlobalSettingsButton(viewModel = viewModel)
                    },
                    windowInsets = WindowInsets(0.dp),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            if (tariffs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Тарифы не найдены", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tariffs, key = { it.id }) { tariff ->
                        TariffItemCard(
                            tariff = tariff,
                            currencyCode = cCode,
                            onEditClick = {
                                selectedTariff = tariff
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }

        // FAB to Add Tariff
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = GymPrimaryIndigo,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить тариф")
        }
    }

    if (showAddDialog) {
        TariffDialog(
            tariff = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, durationType, durationValue, price ->
                viewModel.addMembershipType(name, durationType, durationValue, price)
                showAddDialog = false
            },
            onDelete = { }
        )
    }

    if (showEditDialog && selectedTariff != null) {
        TariffDialog(
            tariff = selectedTariff,
            onDismiss = { showEditDialog = false },
            onSave = { name, durationType, durationValue, price ->
                viewModel.updateMembershipType(selectedTariff!!.id, name, durationType, durationValue, price)
                showEditDialog = false
            },
            onDelete = {
                viewModel.deleteMembershipType(selectedTariff!!.id)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun TariffItemCard(
    tariff: MembershipType,
    currencyCode: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEditClick() }.defaultMinSize(minHeight = 72.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CardMembership,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tariff.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val durationText = if (tariff.durationType == "days") "${tariff.durationValue} дней" else "${tariff.durationValue} визитов"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${tariff.price.toInt()} $currencyCode",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GymPrimaryIndigo
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        durationText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TariffDialog(
    tariff: MembershipType?,
    onDismiss: () -> Unit,
    onSave: (name: String, durationType: String, durationValue: Int, price: Double) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(tariff?.name ?: "") }
    var priceStr by remember { mutableStateOf(tariff?.price?.toString() ?: "") }
    var durationType by remember { mutableStateOf(tariff?.durationType ?: "days") }
    var durationValueStr by remember { mutableStateOf(tariff?.durationValue?.toString() ?: "30") }
    var showDeleteWarning by remember { mutableStateOf(false) }

    if (showDeleteWarning && tariff != null) {
        AlertDialog(
            onDismissRequest = { showDeleteWarning = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Удалить тариф?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Тариф «${tariff.name}» будет удален безвозвратно.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteWarning = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymRoseAlert)
                ) { Text("Удалить", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWarning = false }) { Text("Отмена") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(if (tariff == null) "Создать тариф" else "Настройки тарифа", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название тарифа") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Цена") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Тип срока:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = durationType == "days",
                            onClick = { durationType = "days" },
                            label = { Text("Дни") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GymPrimaryIndigo, selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = durationType == "visits",
                            onClick = { durationType = "visits" },
                            label = { Text("Визиты") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GymPrimaryIndigo, selectedLabelColor = Color.White)
                        )
                    }

                    OutlinedTextField(
                        value = durationValueStr,
                        onValueChange = { durationValueStr = it },
                        label = { Text(if (durationType == "days") "Количество дней" else "Количество визитов") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (tariff != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { showDeleteWarning = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GymRoseAlert)
                        ) {
                            Text("Удалить тариф")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = priceStr.toDoubleOrNull() ?: tariff?.price ?: 0.0
                        val durationVal = durationValueStr.toIntOrNull() ?: tariff?.durationValue ?: 0
                        if (name.isNotBlank() && price > 0 && durationVal > 0) {
                            onSave(name, durationType, durationVal, price)
                        }
                    },
                    enabled = name.isNotBlank() && priceStr.isNotBlank() && durationValueStr.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
                ) {
                    Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        )
    }
}
