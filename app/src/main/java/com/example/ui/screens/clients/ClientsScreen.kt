package com.example.ui.screens.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.example.ui.components.CustomDropdownFilter
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

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Compact Elegant Search Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Sleek Compact Search Field
                    CompactClientSearchField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholderText = "Поиск по имени, коду GT или телефону (+992)..."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom Dropdown & Theme Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomDropdownFilter(
                            options = mapOf(
                                "all" to "Все клиенты (${clients.size})",
                                "visits" to "По визитам",
                                "days" to "По дням",
                                "expiring" to "Истекают / Мало визитов"
                            ),
                            selectedKey = selectedFilterCategory,
                            onItemSelected = { viewModel.selectedFilterCategory.value = it },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        val isSystemDark = isSystemInDarkTheme()
                        val currentIsDark = viewModel.isDarkMode.collectAsState().value ?: isSystemDark

                        IconButton(
                            onClick = { viewModel.toggleTheme(isSystemDark) },
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (currentIsDark) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Клиенты не найдены",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                .padding(20.dp),
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
fun CompactClientSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                if (isSelected) Modifier.border(2.dp, GymPrimaryIndigo, RoundedCornerShape(16.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Client Avatar Initials
            val initials = client.fullName
                .split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")

            Box(
                modifier = Modifier
                    .size(46.dp)
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

            Spacer(modifier = Modifier.width(12.dp))

            // Client Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = client.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = GymIndigoContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = client.clientCode,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GymIndigoOnContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = client.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

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
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = statusColor
                        )
                    }
                } else {
                    Text(
                        text = "Нет активного абонемента",
                        style = MaterialTheme.typography.labelSmall,
                        color = GymRoseAlert
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Quick Check-in Button
            if (active != null) {
                Button(
                    onClick = onDeductVisit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "−1",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }
            } else {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Оформить",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
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
    var phone by remember { mutableStateOf("+992 ") }
    var note by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }

    val phoneRegex = Regex("^\\+992 \\d{2} \\d{3} \\d{4}$")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = GymPrimaryIndigo)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Новый клиент", color = MaterialTheme.colorScheme.onSurface)
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
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        phone = it
                        phoneError = !phone.matches(phoneRegex)
                    },
                    label = { Text("Телефон (+992 98 218 3003) *") },
                    singleLine = true,
                    isError = phoneError && phone.length > 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка (опционально)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            val isFormValid = fullName.isNotBlank() && phone.matches(phoneRegex)
            Button(
                onClick = { 
                    if (isFormValid) {
                        onAddClient(fullName, phone, note.ifBlank { null }) 
                    } else {
                        phoneError = !phone.matches(phoneRegex)
                    }
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
            ) {
                Text("Создать", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
