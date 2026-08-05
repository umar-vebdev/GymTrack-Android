package com.example.ui.screens.products

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Client
import com.example.domain.model.Product
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel

@Composable
fun ProductsScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val categoryFilter by viewModel.productCategoryFilter.collectAsState()
    val searchQuery by viewModel.productSearchQuery.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showQuickSellDialog by remember { mutableStateOf(false) }
    var selectedProductForSale by remember { mutableStateOf<Product?>(null) }

    Box(modifier = modifier.fillMaxSize().background(GymBgLight)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Search and Category Header
            Surface(color = GymSurfaceWhite, shadowElevation = 1.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.productSearchQuery.value = it },
                        placeholder = { Text("Поиск товаров (вода, батончик, протеин)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GymTextSecondary) },
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

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = categoryFilter == "all",
                                onClick = { viewModel.productCategoryFilter.value = "all" },
                                label = { Text("Все товары") },
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
                                selected = categoryFilter == "drinks",
                                onClick = { viewModel.productCategoryFilter.value = "drinks" },
                                label = { Text("Напитки") },
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
                                selected = categoryFilter == "supplements",
                                onClick = { viewModel.productCategoryFilter.value = "supplements" },
                                label = { Text("Спортпит") },
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
                                selected = categoryFilter == "equipment",
                                onClick = { viewModel.productCategoryFilter.value = "equipment" },
                                label = { Text("Экипировка") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GymPrimaryIndigo,
                                    selectedLabelColor = Color.White,
                                    containerColor = GymSurfaceVariant,
                                    labelColor = GymTextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Products List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Товары не найдены", style = MaterialTheme.typography.titleMedium, color = GymTextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onSellClick = {
                                selectedProductForSale = product
                                showQuickSellDialog = true
                            }
                        )
                    }
                }
            }
        }

        // FAB to Add Product
        FloatingActionButton(
            onClick = { showAddProductDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = GymPrimaryIndigo,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.AddShoppingCart, contentDescription = "Add Product")
        }

        if (showAddProductDialog) {
            AddProductDialog(
                onDismiss = { showAddProductDialog = false },
                onAddProduct = { name, category, price, stock ->
                    viewModel.addProduct(name, category, price, stock)
                    showAddProductDialog = false
                }
            )
        }

        if (showQuickSellDialog && selectedProductForSale != null) {
            GeneralSellProductDialog(
                product = selectedProductForSale!!,
                clients = clients,
                onDismiss = { showQuickSellDialog = false },
                onSell = { clientId, qty, method ->
                    viewModel.sellProduct(clientId, selectedProductForSale!!.id, qty, method)
                    showQuickSellDialog = false
                }
            )
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onSellClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GymIndigoContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (product.category) {
                        "drinks" -> Icons.Default.LocalBar
                        "supplements" -> Icons.Default.MedicalServices
                        "equipment" -> Icons.Default.FitnessCenter
                        else -> Icons.Default.ShoppingBag
                    },
                    contentDescription = null,
                    tint = GymPrimaryIndigo
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GymTextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${product.price.toInt()} ₽", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GymPrimaryIndigo)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Запас: ${product.stockQuantity} шт.", style = MaterialTheme.typography.bodySmall, color = GymTextSecondary)
                }
            }

            Button(
                onClick = onSellClick,
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Продать", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onAddProduct: (name: String, category: String, price: Double, stock: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("50") }
    var category by remember { mutableStateOf("drinks") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GymSurfaceWhite,
        title = { Text("Добавить товар в каталог", color = GymTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название товара") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GymBgLight,
                        unfocusedContainerColor = GymBgLight
                    )
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Цена (₽)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GymBgLight,
                        unfocusedContainerColor = GymBgLight
                    )
                )
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Количество на складе") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GymBgLight,
                        unfocusedContainerColor = GymBgLight
                    )
                )

                Text("Категория:", style = MaterialTheme.typography.labelMedium, color = GymTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = category == "drinks",
                        onClick = { category = "drinks" },
                        label = { Text("Напитки") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymPrimaryIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = category == "supplements",
                        onClick = { category = "supplements" },
                        label = { Text("Спортпит") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GymPrimaryIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = category == "equipment",
                        onClick = { category = "equipment" },
                        label = { Text("Инвентарь") },
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
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val stock = stockStr.toIntOrNull() ?: 0
                    onAddProduct(name, category, price, stock)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
            ) {
                Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = GymTextSecondary) } }
    )
}

@Composable
fun GeneralSellProductDialog(
    product: Product,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onSell: (clientId: Long, quantity: Int, paymentMethod: String) -> Unit
) {
    var selectedClientId by remember { mutableLongStateOf(clients.firstOrNull()?.id ?: 1L) }
    var quantity by remember { mutableIntStateOf(1) }
    var paymentMethod by remember { mutableStateOf("cash") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GymSurfaceWhite,
        title = { Text("Продать: ${product.name}", color = GymTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Выберите покупателя:", style = MaterialTheme.typography.labelMedium, color = GymTextSecondary)

                LazyColumn(modifier = Modifier.heightIn(max = 160.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(clients) { c ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(if (selectedClientId == c.id) GymIndigoContainer else GymBgLight)
                                .padding(10.dp),
                            onClick = { selectedClientId = c.id }
                        ) {
                            Text("${c.fullName} (${c.clientCode})", color = GymTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Количество:", color = GymTextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) { Icon(Icons.Default.Remove, contentDescription = null, tint = GymTextPrimary) }
                        Text("$quantity", style = MaterialTheme.typography.titleMedium, color = GymTextPrimary, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { quantity++ }) { Icon(Icons.Default.Add, contentDescription = null, tint = GymTextPrimary) }
                    }
                }

                Text("Сумма: ${(product.price * quantity).toInt()} ₽", style = MaterialTheme.typography.titleLarge, color = GymPrimaryIndigo, fontWeight = FontWeight.Bold)

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
                onClick = { onSell(selectedClientId, quantity, paymentMethod) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
            ) {
                Text("Оформить продажу", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = GymTextSecondary) } }
    )
}
