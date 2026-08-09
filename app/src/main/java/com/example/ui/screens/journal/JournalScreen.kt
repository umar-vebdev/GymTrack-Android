package com.example.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import com.example.domain.model.HistoryEvent
import com.example.ui.theme.*
import com.example.ui.components.FilterChip
import com.example.ui.components.FilterDropdownChip
import com.example.ui.components.GlobalSettingsButton
import com.example.ui.viewmodel.GymViewModel
import com.example.util.formatAsReadableDate

@Composable
fun JournalScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.historyEvents.collectAsState()
    val currentFilters by viewModel.historyTypeFilters.collectAsState()
    val dateFilter by viewModel.historyDateFilter.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val cCode = selectedCurrency?.code ?: "TJS"

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<HistoryEvent?>(null) }

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
                Column {
                    @OptIn(ExperimentalMaterial3Api::class)
                    CenterAlignedTopAppBar(
                        title = { Text("Единый журнал событий", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                        actions = {
                            GlobalSettingsButton(viewModel = viewModel)
                        },
                        windowInsets = WindowInsets(0.dp),
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterLabels.forEach { (key, label) ->
                            val isSelected = currentFilters.contains(key)
                            FilterChip(
                                label = label,
                                isSelected = isSelected,
                                onClick = {
                                    val newSet = if (key == "all") {
                                        setOf("all")
                                    } else {
                                        val mutable = currentFilters.toMutableSet()
                                        mutable.remove("all")
                                        if (mutable.contains(key)) mutable.remove(key) else mutable.add(key)
                                        if (mutable.isEmpty()) setOf("all") else mutable
                                    }
                                    viewModel.historyTypeFilters.value = newSet
                                }
                            )
                        }
                        
                        val optionsWithCustom = dateLabels.toMutableMap()
                        optionsWithCustom["custom_trigger"] = "Свой период..."
                        val displayLabel = if (dateFilter.startsWith("custom|")) "Свой период" else (dateLabels[dateFilter] ?: "Период")
                        FilterDropdownChip(
                            label = displayLabel,
                            options = optionsWithCustom,
                            onItemSelected = { key ->
                                if (key == "custom_trigger") {
                                    showDatePicker = true
                                } else {
                                    viewModel.historyDateFilter.value = key
                                }
                            }
                        )
                    }
                }
            }

            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Событий не найдено", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        EventItemCard(event, cCode, onClick = { selectedEvent = event })
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

    // Event Detail Dialog
    selectedEvent?.let { event ->
        EventDetailDialog(event = event, currencyCode = cCode, onDismiss = { selectedEvent = null })
    }
}

@Composable
fun EventItemCard(event: HistoryEvent, currencyCode: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tint) = when (event) {
                is HistoryEvent.VisitEvent -> Pair(Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary)
                is HistoryEvent.SaleEvent -> Pair(Icons.Default.ShoppingBag, MaterialTheme.colorScheme.primary)
                is HistoryEvent.MembershipEvent -> Pair(Icons.Default.CardMembership, MaterialTheme.colorScheme.primary)
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name + client code badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.clientName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (event.clientCode.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = event.clientCode,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val cleanTitle = event.title.replace(" Продажа абонемента: ", "Абонемент: ").replace(" Продажа товара: ", "Товар: ")
                Text(
                    text = cleanTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = event.timestamp.formatAsReadableDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (event.amount != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+ ${event.amount?.toInt()} $currencyCode",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (event.paymentMethod == "cash") "Наличные" else "Карта",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EventDetailDialog(event: HistoryEvent, currencyCode: String, onDismiss: () -> Unit) {
    val (icon, tint) = when (event) {
        is HistoryEvent.VisitEvent -> Pair(Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary)
        is HistoryEvent.SaleEvent -> Pair(Icons.Default.ShoppingBag, MaterialTheme.colorScheme.primary)
        is HistoryEvent.MembershipEvent -> Pair(Icons.Default.CardMembership, MaterialTheme.colorScheme.primary)
    }

    val cleanTitle = event.title
        .replace(" Продажа абонемента: ", "Абонемент: ")
        .replace(" Продажа товара: ", "Товар: ")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = cleanTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        event.clientName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (event.clientCode.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = event.clientCode,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(label = "Дата и время", value = event.timestamp.formatAsReadableDate())
                if (event.amount != null) {
                    DetailRow(label = "Сумма", value = "${event.amount?.toInt()} $currencyCode")
                    DetailRow(label = "Способ оплаты", value = if (event.paymentMethod == "cash") "Наличные" else "Карта")
                }
                if (event.description.isNotBlank()) {
                    DetailRow(label = "Детали", value = event.description)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}
