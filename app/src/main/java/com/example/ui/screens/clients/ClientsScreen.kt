package com.example.ui.screens.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.domain.model.Client
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel

@Composable
fun ClientsScreen(
    viewModel: GymViewModel,
    onClientSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val clients by viewModel.clients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilterCategory by viewModel.selectedFilterCategory.collectAsState()
    val selectedClientId by viewModel.selectedClientId.collectAsState()

    var showAddClientDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(GymBgLight)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GymSurfaceWhite,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search client, phone or GT-code...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GymTextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GymBgLight,
                            unfocusedContainerColor = GymBgLight,
                            focusedBorderColor = GymPrimaryIndigo,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilterCategory == "all",
                                onClick = { viewModel.selectedFilterCategory.value = "all" },
                                label = { Text("Все клиенты (${clients.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = GymSurfaceVariant,
                                    labelColor = GymTextPrimary
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterCategory == "visits",
                                onClick = { viewModel.selectedFilterCategory.value = "visits" },
                                label = { Text("По визитам") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = GymSurfaceVariant,
                                    labelColor = GymTextPrimary
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterCategory == "days",
                                onClick = { viewModel.selectedFilterCategory.value = "days" },
                                label = { Text("По дням") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = GymSurfaceVariant,
                                    labelColor = GymTextPrimary
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilterCategory == "expiring",
                                onClick = { viewModel.selectedFilterCategory.value = "expiring" },
                                label = { Text("Истекают / Мало визитов") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymAmberAlert,
                                    selectedLabelColor = Color.White,
                                    containerColor = GymSurfaceVariant,
                                    labelColor = GymTextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Clients List
            if (clients.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = GymTextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Клиенты не найдены",
                            style = MaterialTheme.typography.titleMedium,
                            color = GymTextSecondary
                        )
                    }
                }
            } else {
                LazyLazyClientsList(
                    clients = clients,
                    selectedClientId = selectedClientId,
                    onClientClick = { client ->
                        viewModel.selectClient(client.id)
                        onClientSelected(client.id)
                    },
                    onDeductVisit = { clientId ->
                        viewModel.deductVisit(clientId)
                    }
                )
            }
        }

        // FAB - Add Client
        FloatingActionButton(
            onClick = { showAddClientDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = GymPrimaryIndigo,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add Client")
        }

        if (showAddClientDialog) {
            AddClientDialog(
                onDismiss = { showAddClientDialog = false },
                onAddClient = { fullName, phone, note ->
                    viewModel.addClient(fullName, phone, note)
                    showAddClientDialog = false
                }
            )
        }
    }
}

@Composable
fun LazyLazyClientsList(
    clients: List<Client>,
    selectedClientId: Long?,
    onClientClick: (Client) -> Unit,
    onDeductVisit: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(clients, key = { it.id }) { client ->
            ClientCardItem(
                client = client,
                isSelected = client.id == selectedClientId,
                onClick = { onClientClick(client) },
                onDeductVisit = { onDeductVisit(client.id) }
            )
        }
    }
}

@Composable
fun ClientCardItem(
    client: Client,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeductVisit: () -> Unit
) {
    val active = client.activeMembership

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, GymPrimaryIndigo, RoundedCornerShape(20.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Client Avatar Circle with Initials
            val initials = client.fullName
                .split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GymIndigoContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.ifEmpty { "GT" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GymPrimaryIndigo
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Client Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = client.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GymTextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Client Code Badge
                    Surface(
                        color = GymIndigoContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = client.clientCode,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GymIndigoOnContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = client.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GymTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tariff Status Badge
                if (active != null) {
                    val statusColor = if (active.isExpired) GymRoseAlert else if (active.visitsLeft != null && active.visitsLeft <= 2) GymAmberAlert else GymGreenSuccess
                    val text = if (active.durationType == "visits") {
                        " ${active.tariffName} • Осталось ${active.visitsLeft ?: 0} визитов"
                    } else {
                        " ${active.tariffName} • До ${active.expiresAt ?: ""}"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = statusColor
                        )
                    }
                } else {
                    Text(
                        text = "Нет активного абонемента",
                        style = MaterialTheme.typography.bodySmall,
                        color = GymRoseAlert
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Quick Check-in Button right on the card!
            Button(
                onClick = onDeductVisit,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "−1",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AddClientDialog(
    onDismiss: () -> Unit,
    onAddClient: (String, String, String?) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+7 ") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GymSurfaceWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = GymPrimaryIndigo)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Новый клиент", color = GymTextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("ФИО Клиента *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = GymBgLight,
                        focusedContainerColor = GymSurfaceWhite
                    )
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Номер телефона *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = GymBgLight,
                        focusedContainerColor = GymSurfaceWhite
                    )
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка (опционально)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = GymBgLight,
                        focusedContainerColor = GymSurfaceWhite
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddClient(fullName, phone, note.ifBlank { null }) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
            ) {
                Text("Создать", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = GymTextSecondary)
            }
        }
    )
}
