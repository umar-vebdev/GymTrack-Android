package com.example.ui.screens.tariffs

import androidx.compose.foundation.background
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
import com.example.ui.viewmodel.GymViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TariffsScreen(
    viewModel: GymViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tariffs by viewModel.membershipTypesAdmin.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTariff by remember { mutableStateOf<MembershipType?>(null) }
    
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление тарифами", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GymPrimaryIndigo,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить тариф")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (tariffs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Тарифы не найдены", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tariffs, key = { it.id }) { tariff ->
                        TariffItemCard(
                            tariff = tariff,
                            onEditClick = {
                                selectedTariff = tariff
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
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
            onToggleActive = { }
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
            onToggleActive = {
                viewModel.toggleMembershipTypeActive(selectedTariff!!.id)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun TariffItemCard(
    tariff: MembershipType,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (tariff.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (tariff.isActive) GymIndigoContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CardMembership,
                    contentDescription = null,
                    tint = if (tariff.isActive) GymPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tariff.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    if (!tariff.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = GymRoseAlert, shape = RoundedCornerShape(6.dp)) {
                            Text("Выкл", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val durationText = if (tariff.durationType == "days") "${tariff.durationValue} дней" else "${tariff.durationValue} визитов"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${tariff.price.toInt()} сомони", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (tariff.isActive) GymPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(durationText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TariffDialog(
    tariff: MembershipType?,
    onDismiss: () -> Unit,
    onSave: (name: String, durationType: String, durationValue: Int, price: Double) -> Unit,
    onToggleActive: () -> Unit
) {
    var name by remember { mutableStateOf(tariff?.name ?: "") }
    var priceStr by remember { mutableStateOf(tariff?.price?.toString() ?: "") }
    var durationType by remember { mutableStateOf(tariff?.durationType ?: "days") }
    var durationValueStr by remember { mutableStateOf(tariff?.durationValue?.toString() ?: "30") }

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
                    label = { Text("Цена (сомони)") },
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
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onToggleActive,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (tariff.isActive) GymRoseAlert else GymGreenSuccess)
                    ) {
                        Text(if (tariff.isActive) "Деактивировать тариф" else "Активировать тариф")
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
