package com.example.ui.screens.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.util.formatAsReadableDate

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
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val cCode = selectedCurrency?.code ?: "TJS"

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showPurchaseTariffModal by remember { mutableStateOf(false) }
    var showSellProductModal by remember { mutableStateOf(false) }
    var showDeleteClientWarning by remember { mutableStateOf(false) }

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

            if (showDeleteClientWarning) {
                AlertDialog(
                    onDismissRequest = { showDeleteClientWarning = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = { Text("Удалить клиента?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    text = { Text("Клиент «${client.fullName}» будет удален безвозвратно.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    confirmButton = {
                        Button(
                            onClick = { 
                                showDeleteClientWarning = false
                                viewModel.deleteClient(client.id)
                                if (onBackClick != null) onBackClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GymRoseAlert)
                        ) { Text("Удалить", color = Color.White, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = { TextButton(onClick = { showDeleteClientWarning = false }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Client Profile Header Card
                item {
                    ClientHeaderCard(
                        client = client,
                        onDeleteClick = { showDeleteClientWarning = true }
                    )
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
                            item { TabPurchasesView(purchases, cCode) }
                        }
                    }
                    2 -> {
                        if (sales.isEmpty()) {
                            item { EmptyStateText("Покупок товаров нет") }
                        } else {
                            item { TabSalesView(sales, cCode) }
                        }
                    }
                }
            }
        }

        // Modals
        if (showPurchaseTariffModal) {
            SellMembershipDialog(
                client = client,
                membershipTypes = membershipTypes,
                currencyCode = cCode,
                onDismiss = { showPurchaseTariffModal = false },
                onSell = { typeId, method ->
                    viewModel.purchaseMembership(client.id, typeId, method)
                    showPurchaseTariffModal = false
                }
            )
        }

        if (showSellProductModal) {
            SellProductDialog(
                client = client,
                products = products,
                currencyCode = cCode,
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
fun ClientHeaderCard(client: Client, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initials = client.fullName
                .split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.ifEmpty { "GT" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = GymPrimaryIndigo
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.fullName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = client.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(color = GymIndigoContainer, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "Код: ${client.clientCode}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GymIndigoOnContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (!client.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Заметка: ${client.note}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GymAmberAlert
                    )
                }
            }

            IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить клиента", tint = GymRoseAlert, modifier = Modifier.size(20.dp))
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
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = "ТЕКУЩИЙ АБОНЕМЕНТ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (active != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = active.tariffName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        if (active.durationType == "visits") {
                            val count = active.visitsLeft ?: 0
                            Text(
                                text = "Осталось визитов: $count из ${active.totalVisits ?: 12}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (count <= 2) GymAmberAlert else GymGreenSuccess
                            )
                        } else {
                            Text(
                                text = "Действителен до: ${active.expiresAt?.formatAsReadableDate() ?: "—"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (active.isExpired) GymRoseAlert else GymGreenSuccess
                            )
                        }
                    }

                    Surface(
                        color = if (active.isExpired) GymRoseAlert.copy(alpha = 0.15f) else GymGreenSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (active.isExpired) "ИСТЕК" else "АКТИВЕН",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (active.isExpired) GymRoseAlert else GymGreenSuccess,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDeductVisit,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "−1 ПОСЕЩЕНИЕ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }

            } else {
                Text(
                    text = "У клиента нет активного абонемента",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GymRoseAlert
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onRenewClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
                ) {
                    Icon(
                        imageVector = Icons.Default.CardMembership,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ОФОРМИТЬ АБОНЕМЕНТ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSellProductClick,
                modifier = Modifier.fillMaxWidth().height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GymPrimaryIndigo)
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Продать товар", style = MaterialTheme.typography.bodySmall)
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
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GymGreenSuccess.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = GymGreenSuccess, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Посещение зала", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(visit.visitedAt.formatAsReadableDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = GymRoseAlert, modifier = Modifier.size(16.dp))
            }
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Удаление посещения", fontWeight = FontWeight.Bold) },
            text = { Text("Вы точно хотите удалить отметку о посещении? Одно занятие будет возвращено на баланс активного абонемента.") },
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
fun TabPurchasesView(
    purchaseHistory: List<MembershipPurchase>,
    currencyCode: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        purchaseHistory.forEach { purchase ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CardMembership, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = purchase.membershipTypeName, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Сумма: ${purchase.amountPaid.toInt()} $currencyCode (${if (purchase.paymentMethod == "cash") "Наличные" else "Карта"})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("C ${purchase.startsAt.formatAsReadableDate()} до ${purchase.expiresAt?.formatAsReadableDate() ?: "—"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun TabSalesView(
    salesHistory: List<ProductSale>,
    currencyCode: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        salesHistory.forEach { sale ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GymAmberAlert.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = GymAmberAlert, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${sale.productName} (x${sale.quantity})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${sale.totalPrice.toInt()} $currencyCode (${if (sale.paymentMethod == "cash") "Наличные" else "Карта"})", style = MaterialTheme.typography.labelSmall, color = GymGreenSuccess)
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellMembershipDialog(
    client: Client,
    membershipTypes: List<com.example.domain.model.MembershipType>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSell: (typeId: Long, paymentMethod: String) -> Unit
) {
    var selectedTypeId by remember { mutableLongStateOf(membershipTypes.firstOrNull()?.id ?: 1L) }
    var paymentMethod by remember { mutableStateOf("card") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Оформление абонемента", 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text("Клиент: ${client.fullName}", style = MaterialTheme.typography.titleMedium, color = GymPrimaryIndigo, fontWeight = FontWeight.SemiBold)

            Text("Выберите тариф:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            LazyColumn(
                modifier = Modifier.heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(membershipTypes) { type ->
                    val isSelected = selectedTypeId == type.id
                    Card(
                        onClick = { selectedTypeId = type.id },
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(type.name, style = MaterialTheme.typography.bodyMedium, color = GymTextPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${type.durationValue} ${if (type.durationType == "visits") "визитов" else "дней"}", style = MaterialTheme.typography.labelSmall, color = GymTextSecondary)
                            }
                            Text("${type.price.toInt()} $currencyCode", style = MaterialTheme.typography.bodyMedium, color = GymPrimaryIndigo, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text("Способ оплаты:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentMethodButton(
                    text = "Наличные",
                    isSelected = paymentMethod == "cash",
                    onClick = { paymentMethod = "cash" },
                    modifier = Modifier.weight(1f)
                )
                PaymentMethodButton(
                    text = "Карта",
                    isSelected = paymentMethod == "card",
                    onClick = { paymentMethod = "card" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onSell(selectedTypeId, paymentMethod) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
            ) {
                Text("ОПЛАТИТЬ И ПРОДЛИТЬ", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellProductDialog(
    client: Client,
    products: List<Product>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSell: (productId: Long, quantity: Int, paymentMethod: String) -> Unit
) {
    var selectedProductId by remember { mutableLongStateOf(products.firstOrNull()?.id ?: 1L) }
    var quantity by remember { mutableIntStateOf(1) }
    var paymentMethod by remember { mutableStateOf("cash") }
    
    val currentProduct = products.find { it.id == selectedProductId }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Продажа товара", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text("Клиент: ${client.fullName}", style = MaterialTheme.typography.titleMedium, color = GymPrimaryIndigo, fontWeight = FontWeight.SemiBold)

            Text("Выберите товар:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            LazyColumn(
                modifier = Modifier.heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { prod ->
                    val isSelected = selectedProductId == prod.id
                    Card(
                        onClick = { selectedProductId = prod.id },
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prod.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("${prod.price.toInt()} $currencyCode", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Количество:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("$quantity", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))

            Text("Способ оплаты:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentMethodButton(
                    text = "Наличные",
                    isSelected = paymentMethod == "cash",
                    onClick = { paymentMethod = "cash" },
                    modifier = Modifier.weight(1f)
                )
                PaymentMethodButton(
                    text = "Карта",
                    isSelected = paymentMethod == "card",
                    onClick = { paymentMethod = "card" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val total = (currentProduct?.price ?: 0.0) * quantity
            Button(
                onClick = { onSell(selectedProductId, quantity, paymentMethod) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
            ) {
                Text("ОПЛАТИТЬ: ${total.toInt()} ${currencyCode.uppercase()}", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.height(48.dp).then(if(isSelected) Modifier.border(2.dp, GymPrimaryIndigo, RoundedCornerShape(12.dp)) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) GymPrimaryIndigo.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = if (isSelected) GymPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
