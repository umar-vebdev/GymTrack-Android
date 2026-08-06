package com.example.ui.screens.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.domain.model.MembershipPurchase
import com.example.domain.model.Product
import com.example.domain.model.ProductSale
import com.example.domain.model.Visit
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel

@Composable
fun ClientDetailScreen(
    viewModel: GymViewModel,
    client: Client?,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (client == null) {
        Box(
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Выберите клиента из списка", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val visits by viewModel.selectedClientVisits.collectAsState()
    val purchases by viewModel.selectedClientPurchases.collectAsState()
    val sales by viewModel.selectedClientSales.collectAsState()
    val membershipTypes by viewModel.membershipTypes.collectAsState()
    val products by viewModel.products.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showPurchaseTariffModal by remember { mutableStateOf(false) }
    var showSellProductModal by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar if on phone (with back arrow)
            if (onBackClick != null) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = "Карточка клиента",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Client Profile Header Card
                item {
                    ClientHeaderCard(client = client)
                }

                // 2. Active Membership Card with -1 Visit Action
                item {
                    ActiveMembershipSection(
                        client = client,
                        onDeductVisit = { viewModel.deductVisit(client.id) },
                        onRenewClick = { showPurchaseTariffModal = true },
                        onSellProductClick = { showSellProductModal = true }
                    )
                }

                // 3. Three Sub-Tabs (Visits, Memberships, Product Purchases)
                item {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = GymPrimaryIndigo,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Визиты (${visits.size})", color = if (selectedTabIndex == 0) GymPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Абонементы (${purchases.size})", color = if (selectedTabIndex == 1) GymPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            text = { Text("Товары (${sales.size})", color = if (selectedTabIndex == 2) GymPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                    }
                }

                // Sub-Tab Content
                when (selectedTabIndex) {
                    0 -> {
                        if (visits.isEmpty()) {
                            item { EmptyStateText("История визитов пуста") }
                        } else {
                            items(visits) { visit -> 
                                VisitHistoryItem(visit) {
                                    viewModel.cancelCheckIn(visit.id)
                                }
                            }
                        }
                    }
                    1 -> {
                        if (purchases.isEmpty()) {
                            item { EmptyStateText("Купленных абонементов нет") }
                        } else {
                            items(purchases) { purchase -> 
                                PurchaseHistoryItem(purchase, client)
                            }
                        }
                    }
                    2 -> {
                        if (sales.isEmpty()) {
                            item { EmptyStateText("Покупок товаров нет") }
                        } else {
                            items(sales) { sale -> ProductSaleHistoryItem(sale) }
                        }
                    }
                }
            }
        }

        // Modals
        if (showPurchaseTariffModal) {
            PurchaseTariffModal(
                client = client,
                membershipTypes = membershipTypes,
                onDismiss = { showPurchaseTariffModal = false },
                onPurchase = { typeId, method ->
                    viewModel.purchaseMembership(client.id, typeId, method)
                    showPurchaseTariffModal = false
                }
            )
        }

        if (showSellProductModal) {
            SellProductModal(
                client = client,
                products = products,
                onDismiss = { showSellProductModal = false },
                onSell = { productId, qty, method ->
                    viewModel.sellProduct(client.id, productId, qty, method)
                    showSellProductModal = false
                }
            )
        }
    }
}

@Composable
fun ClientHeaderCard(client: Client) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initials = client.fullName
                .split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GymIndigoContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.ifEmpty { "GT" },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = GymPrimaryIndigo
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.fullName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = client.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(color = GymIndigoContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "Код: ${client.clientCode}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = GymIndigoOnContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (!client.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Заметка: ${client.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GymAmberAlert
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveMembershipSection(
    client: Client,
    onDeductVisit: () -> Unit,
    onRenewClick: () -> Unit,
    onSellProductClick: () -> Unit
) {
    val active = client.activeMembership

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = "ТЕКУЩИЙ АБОНЕМЕНТ",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (active != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = active.tariffName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (active.durationType == "visits") {
                            val count = active.visitsLeft ?: 0
                            Text(
                                text = "Осталось визитов: $count из ${active.totalVisits ?: 12}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (count <= 2) GymAmberAlert else GymGreenSuccess
                            )
                        } else {
                            Text(
                                text = "Действителен до: ${active.expiresAt}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (active.isExpired) GymRoseAlert else GymGreenSuccess
                            )
                        }
                    }

                    Surface(
                        color = if (active.isExpired) GymRoseAlert.copy(alpha = 0.15f) else GymGreenContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (active.isExpired) "ИСТЕК" else "АКТИВЕН",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (active.isExpired) GymRoseAlert else GymGreenSuccess,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Prominent Big "-1 Посещение" Button
                Button(
                    onClick = onDeductVisit,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "−1 ПОСЕЩЕНИЕ (ЧЕК-ИН)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }

            } else {
                Text(
                    text = "У клиента нет активного абонемента",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GymRoseAlert
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Modals Trigger Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (active == null) {
                    OutlinedButton(
                        onClick = onRenewClick,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GymPrimaryIndigo)
                    ) {
                        Icon(Icons.Default.CardMembership, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Оформить")
                    }
                }

                OutlinedButton(
                    onClick = onSellProductClick,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GymPrimaryIndigo)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Продать товар")
                }
            }
        }
    }
}

@Composable
fun VisitHistoryItem(visit: Visit, onCancel: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = GymGreenSuccess)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Посещение зала", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(visit.visitedAt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = { showDialog = true },
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = GymRoseAlert, modifier = Modifier.size(20.dp))
            }
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Отмена чек-ина", fontWeight = FontWeight.Bold) },
            text = { Text("Вы точно хотите отменить этот визит? Одно посещение будет возвращено на баланс активного абонемента.") },
            confirmButton = {
                Button(
                    onClick = { 
                        onCancel()
                        showDialog = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymRoseAlert)
                ) { Text("Удалить", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun PurchaseHistoryItem(purchase: MembershipPurchase, client: Client) {
    val isActive = client.activeMembership?.id == purchase.id

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isActive) GymIndigoContainer else MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CardMembership, contentDescription = null, tint = if (isActive) GymPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = purchase.membershipTypeName, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = if (isActive) GymPrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
                Text("Сумма: ${purchase.amountPaid.toInt()} сомони (${if (purchase.paymentMethod == "cash") "Наличные" else "Карта"})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("C ${purchase.startsAt} до ${purchase.expiresAt ?: "—"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isActive) {
                Surface(
                    color = GymPrimaryIndigo,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "АКТИВНЫЙ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductSaleHistoryItem(sale: ProductSale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = GymAmberAlert)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${sale.productName} (x${sale.quantity})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("${sale.totalPrice.toInt()} сомони (${if (sale.paymentMethod == "cash") "Наличные" else "Карта"})", style = MaterialTheme.typography.bodySmall, color = GymGreenSuccess)
            }
        }
    }
}

@Composable
fun EmptyStateText(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Modal - Purchase Tariff
@Composable
fun PurchaseTariffModal(
    client: Client,
    membershipTypes: List<com.example.domain.model.MembershipType>,
    onDismiss: () -> Unit,
    onPurchase: (typeId: Long, paymentMethod: String) -> Unit
) {
    var selectedTypeId by remember { mutableLongStateOf(membershipTypes.firstOrNull()?.id ?: 1L) }
    var paymentMethod by remember { mutableStateOf("card") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Оформление абонемента", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Клиент: ${client.fullName}", style = MaterialTheme.typography.bodyMedium, color = GymPrimaryIndigo, fontWeight = FontWeight.Bold)

                Text("Выберите тариф:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                membershipTypes.forEach { type ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTypeId == type.id) GymIndigoContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        onClick = { selectedTypeId = type.id }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(type.name, style = MaterialTheme.typography.titleSmall, color = GymTextPrimary, fontWeight = FontWeight.Bold)
                                Text("${type.durationValue} ${if (type.durationType == "visits") "визитов" else "дней"}", style = MaterialTheme.typography.bodySmall, color = GymTextSecondary)
                            }
                            Text("${type.price.toInt()} сомони", style = MaterialTheme.typography.titleMedium, color = GymPrimaryIndigo, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("Способ оплаты:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMethod == "card",
                        onClick = { paymentMethod = "card" },
                        label = { Text("Карта") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymPrimaryIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = paymentMethod == "cash",
                        onClick = { paymentMethod = "cash" },
                        label = { Text("Наличные") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymPrimaryIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPurchase(selectedTypeId, paymentMethod) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
            ) {
                Text("Оплатить и продлить", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

// Modal - Sell Product to Client
@Composable
fun SellProductModal(
    client: Client,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSell: (productId: Long, quantity: Int, paymentMethod: String) -> Unit
) {
    var selectedProductId by remember { mutableLongStateOf(products.firstOrNull()?.id ?: 1L) }
    var quantity by remember { mutableIntStateOf(1) }
    var paymentMethod by remember { mutableStateOf("cash") }

    val currentProduct = products.find { it.id == selectedProductId }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Продажа товара клиенту", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Клиент: ${client.fullName}", style = MaterialTheme.typography.bodyMedium, color = GymPrimaryIndigo, fontWeight = FontWeight.Bold)

                Text("Выберите товар:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                LazyColumn(
                    modifier = Modifier.heightIn(max = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(products) { prod ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedProductId == prod.id) GymIndigoContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            onClick = { selectedProductId = prod.id }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(prod.name, style = MaterialTheme.typography.bodyMedium, color = GymTextPrimary, fontWeight = FontWeight.Bold)
                                Text("${prod.price.toInt()} сомони", style = MaterialTheme.typography.bodyMedium, color = GymPrimaryIndigo, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Количество:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("$quantity", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { quantity++ }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                val total = (currentProduct?.price ?: 0.0) * quantity
                Text(
                    text = "Итого к оплате: ${total.toInt()} сомони",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = GymPrimaryIndigo
                )

                Text("Способ оплаты:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMethod == "cash",
                        onClick = { paymentMethod = "cash" },
                        label = { Text("Наличные") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymPrimaryIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = paymentMethod == "card",
                        onClick = { paymentMethod = "card" },
                        label = { Text("Карта") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymPrimaryIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSell(selectedProductId, quantity, paymentMethod) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
            ) {
                Text("Продать", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}
