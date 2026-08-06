package com.example.ui.screens.analytics

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ExpiringMembershipInfo
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val dateFilter by viewModel.analyticsDateFilter.collectAsState()
    val expiringList by viewModel.expiringMemberships.collectAsState()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header & Date Range Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Аналитика и финансовый отчет",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Date Range Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = dateFilter == "today",
                                onClick = { viewModel.analyticsDateFilter.value = "today" },
                                label = { Text("Сегодня", fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = dateFilter == "yesterday",
                                onClick = { viewModel.analyticsDateFilter.value = "yesterday" },
                                label = { Text("Вчера", fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = dateFilter == "week",
                                onClick = { viewModel.analyticsDateFilter.value = "week" },
                                label = { Text("7 дней", fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = dateFilter == "month",
                                onClick = { viewModel.analyticsDateFilter.value = "month" },
                                label = { Text("30 дней", fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = dateFilter == "all",
                                onClick = { viewModel.analyticsDateFilter.value = "all" },
                                label = { Text("Всё время", fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = dateFilter.startsWith("custom|"),
                                onClick = { showDatePicker = true },
                                label = { Text(if (dateFilter.startsWith("custom|")) "Выбран период" else "Свой период", fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            // 1. Compact Dashboard Stat Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Выручка",
                            value = "${stats.todayRevenue.toInt()} сомони",
                            icon = Icons.Default.Payments,
                            accentColor = GymPrimaryIndigo,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Визиты",
                            value = "${stats.todayVisits} чел.",
                            icon = Icons.Default.CheckCircle,
                            accentColor = GymGreenSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Продажи товаров",
                            value = "${stats.todaySalesCount} шт.",
                            icon = Icons.Default.ShoppingBag,
                            accentColor = GymAmberAlert,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Активные тарифы",
                            value = "${stats.activeMemberships}",
                            icon = Icons.Default.CardMembership,
                            accentColor = GymPrimaryIndigo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Revenue Payment Method Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "СТРУКТУРА ОПЛАТ",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val total = (stats.cashRevenue + stats.cardRevenue).coerceAtLeast(1.0)
                        val cashPct = (stats.cashRevenue / total).toFloat()
                        val cardPct = (stats.cardRevenue / total).toFloat()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (cashPct > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(cashPct.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(GymGreenSuccess)
                                )
                            }
                            if (cardPct > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(cardPct.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(GymPrimaryIndigo)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(GymGreenSuccess))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Наличные: ${stats.cashRevenue.toInt()} сомони", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(GymPrimaryIndigo))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Карта: ${stats.cardRevenue.toInt()} сомони", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // 3. Expiring Memberships Header (Clean & Balanced)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = GymAmberAlert,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Истекающие абонементы",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Surface(
                        color = GymAmberAlert.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${expiringList.size} клинт.",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GymAmberAlert,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (expiringList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Все абонементы активны и действительны", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(expiringList) { expiring ->
                    ExpiringClientCard(
                        info = expiring,
                        onCallClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${expiring.phone}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    )
                }
            }
        }
        
        if (showDatePicker) {
            CustomDateRangePickerModal(
                onDismiss = { showDatePicker = false },
                onDateRangeSelected = { start, end ->
                    viewModel.analyticsDateFilter.value = "custom|$start|$end"
                    showDatePicker = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePickerModal(
    onDismiss: () -> Unit,
    onDateRangeSelected: (String, String) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startMillis = dateRangePickerState.selectedStartDateMillis
                    val endMillis = dateRangePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val startStr = sdf.format(Date(startMillis))
                        val endStr = sdf.format(Date(endMillis))
                        onDateRangeSelected(startStr, endStr)
                    }
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text("Выбрать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(text = "Выберите диапазон", modifier = Modifier.padding(16.dp))
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun ExpiringClientCard(
    info: ExpiringMembershipInfo,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GymAmberAlert.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = GymAmberAlert, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(info.clientName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(info.tariffName, style = MaterialTheme.typography.bodySmall, color = GymPrimaryIndigo, fontWeight = FontWeight.Bold)

                val reason = if (info.visitsLeft != null) {
                    "Осталось визитов: ${info.visitsLeft}"
                } else {
                    "Осталось дней: ${info.daysLeft ?: 0}"
                }
                Text(reason, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = GymAmberAlert)
            }

            IconButton(
                onClick = onCallClick,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GymIndigoContainer)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = GymPrimaryIndigo, modifier = Modifier.size(18.dp))
            }
        }
    }
}
