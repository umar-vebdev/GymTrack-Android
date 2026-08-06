package com.example.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.domain.model.HistoryEvent
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel
import com.example.util.formatAsReadableDate

@Composable
fun JournalScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.historyEvents.collectAsState()
    val currentFilter by viewModel.historyTypeFilter.collectAsState()
    val dateFilter by viewModel.historyDateFilter.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedTypeDropdown by remember { mutableStateOf(false) }
    var expandedDateDropdown by remember { mutableStateOf(false) }

    val filterLabels = mapOf(
        "all" to "Все события",
        "visit" to "Посещения",
        "product_sale" to "Продажи товаров",
        "membership_purchase" to "Абонементы"
    )

    val dateLabels = mapOf(
        "today" to "Сегодня",
        "yesterday" to "Вчера",
        "week" to "За неделю",
        "month" to "За месяц",
        "all" to "Всё время"
    )

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header & Filters
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Column(modifier = Modifier.statusBarsPadding().padding(16.dp)) {
                    Text(
                        text = "Единый журнал событий",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date Filter Dropdown/Button
                        Box {
                            OutlinedButton(
                                onClick = { expandedDateDropdown = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (dateFilter.startsWith("custom|")) "Свой период" else (dateLabels[dateFilter] ?: "Период"),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            
                            DropdownMenu(
                                expanded = expandedDateDropdown,
                                onDismissRequest = { expandedDateDropdown = false }
                            ) {
                                dateLabels.forEach { (key, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = { 
                                            viewModel.historyDateFilter.value = key
                                            expandedDateDropdown = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Свой период...") },
                                    onClick = {
                                        expandedDateDropdown = false
                                        showDatePicker = true
                                    }
                                )
                            }
                        }

                        // Type Filter Dropdown/Button
                        Box {
                            OutlinedButton(
                                onClick = { expandedTypeDropdown = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(filterLabels[currentFilter] ?: "Все события", maxLines = 1)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            
                            DropdownMenu(
                                expanded = expandedTypeDropdown,
                                onDismissRequest = { expandedTypeDropdown = false }
                            ) {
                                filterLabels.forEach { (key, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = { 
                                            viewModel.historyTypeFilter.value = key
                                            expandedTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Events Timeline
            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Событий не найдено", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        HistoryEventCard(event)
                    }
                }
            }
        }
        
        if (showDatePicker) {
            com.example.ui.screens.analytics.CustomDateRangePickerModal(
                onDismiss = { showDatePicker = false },
                onDateRangeSelected = { start, end ->
                    viewModel.historyDateFilter.value = "custom|$start|$end"
                    showDatePicker = false
                }
            )
        }
    }
}

@Composable
fun HistoryEventCard(event: HistoryEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, iconTint) = when (event) {
                is HistoryEvent.VisitEvent -> Pair(Icons.Default.CheckCircle, GymGreenSuccess)
                is HistoryEvent.SaleEvent -> Pair(Icons.Default.ShoppingBag, GymAmberAlert)
                is HistoryEvent.MembershipEvent -> Pair(Icons.Default.CardMembership, GymPrimaryIndigo)
            }

            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Клиент: ${event.clientName}", style = MaterialTheme.typography.bodySmall, color = GymPrimaryIndigo, fontWeight = FontWeight.Bold)
                Text(event.timestamp.formatAsReadableDate(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = GymGreenSuccess)
                Text(event.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (event.amount != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("+${event.amount?.toInt()} сомони", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GymGreenSuccess)
                    Text(if (event.paymentMethod == "cash") "Наличные" else "Карта", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
