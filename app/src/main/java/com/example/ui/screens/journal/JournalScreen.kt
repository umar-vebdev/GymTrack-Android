package com.example.ui.screens.journal

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.HistoryEvent
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel

@Composable
fun JournalScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.historyEvents.collectAsState()
    val currentFilter by viewModel.historyTypeFilter.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header & Filters
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Единый журнал событий",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = currentFilter == "all",
                                onClick = { viewModel.historyTypeFilter.value = "all" },
                                label = { Text("Все события") },
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
                                selected = currentFilter == "visit",
                                onClick = { viewModel.historyTypeFilter.value = "visit" },
                                label = { Text("Чек-ины") },
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
                                selected = currentFilter == "product_sale",
                                onClick = { viewModel.historyTypeFilter.value = "product_sale" },
                                label = { Text("Продажи товаров") },
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
                                selected = currentFilter == "membership_purchase",
                                onClick = { viewModel.historyTypeFilter.value = "membership_purchase" },
                                label = { Text("Абонементы") },
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
