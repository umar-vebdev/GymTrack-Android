package com.example.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.domain.model.Product
import com.example.ui.components.CustomDropdownFilter
import com.example.ui.components.FilterChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel

import com.example.ui.components.GlobalSettingsButton

@Composable
fun ProductsScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val categoryFilters by viewModel.productCategoryFilters.collectAsState()
    val searchQuery by viewModel.productSearchQuery.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val cCode = selectedCurrency?.code ?: "TJS"

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showEditProductDialog by remember { mutableStateOf(false) }
    var showQuickSellDialog by remember { mutableStateOf(false) }
    var selectedProductForSale by remember { mutableStateOf<Product?>(null) }
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Search and Category Header
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Column {
                    @OptIn(ExperimentalMaterial3Api::class)
                    CenterAlignedTopAppBar(
                        title = { Text("Товары", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                        actions = {
                            GlobalSettingsButton(viewModel = viewModel)
                        },
                        windowInsets = WindowInsets(0.dp),
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        // Sleek Compact Product Search Bar
                        CompactProductSearchField(
                            value = searchQuery,
                            onValueChange = { viewModel.productSearchQuery.value = it },
                            placeholderText = "Поиск по каталогу товаров..."
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                    val categoryOptions = mapOf(
                        "all" to "Все товары",
                        "drinks" to "Напитки",
                        "supplements" to "Спортпит",
                        "equipment" to "Экипировка"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryOptions.forEach { (key, label) ->
                            val isSelected = categoryFilters.contains(key)
                            FilterChip(
                                label = label,
                                isSelected = isSelected,
                                onClick = {
                                    val newSet = if (key == "all") {
                                        setOf("all")
                                    } else {
                                        val mutable = categoryFilters.toMutableSet()
                                        mutable.remove("all")
                                        if (mutable.contains(key)) mutable.remove(key) else mutable.add(key)
                                        if (mutable.isEmpty()) setOf("all") else mutable
                                    }
                                    viewModel.productCategoryFilters.value = newSet
                                }
                            )
                        }
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
                    Text("Товары не найдены", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            currencyCode = cCode,
                            onSellClick = {
                                selectedProductForSale = product
                                showQuickSellDialog = true
                            },
                            onEditClick = {
                                selectedProductForEdit = product
                                showEditProductDialog = true
                            }
                        )
                    }
                }
            }
        }

        // FAB to Add Product
        FloatingActionButton(
            onClick = { showAddProductDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
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
                currencyCode = cCode,
                clients = clients,
                onDismiss = { showQuickSellDialog = false },
                onSell = { clientId, qty, method ->
                    viewModel.sellProduct(clientId, selectedProductForSale!!.id, qty, method)
                    showQuickSellDialog = false
                }
            )
        }
        
        if (showEditProductDialog && selectedProductForEdit != null) {
            EditProductDialog(
                product = selectedProductForEdit!!,
                onDismiss = { showEditProductDialog = false },
                onSave = { name, cat, price, stock ->
                    viewModel.updateProduct(selectedProductForEdit!!.id, name, cat, price, stock)
                    showEditProductDialog = false
                },
                onDelete = {
                    viewModel.deleteProduct(selectedProductForEdit!!.id)
                    showEditProductDialog = false
                }
            )
        }
    }
}

@Composable
fun CompactProductSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(44.dp),
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
fun ProductItemCard(
    product: Product,
    currencyCode: String,
    onSellClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEditClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${product.price.toInt()} $currencyCode",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GymPrimaryIndigo
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Склад: ${product.stockQuantity} шт.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                onClick = onSellClick,
                enabled = product.stockQuantity > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Продать", color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
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
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Добавить товар в каталог", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название товара") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Цена") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Количество на складе") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Категория:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CustomDropdownFilter(
                    options = mapOf(
                        "drinks" to "Напитки",
                        "supplements" to "Спортпит",
                        "equipment" to "Инвентарь"
                    ),
                    selectedKey = category,
                    onItemSelected = { category = it },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, price: Double, stock: Int) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var priceStr by remember { mutableStateOf(product.price.toString()) }
    var stockStr by remember { mutableStateOf(product.stockQuantity.toString()) }
    var category by remember { mutableStateOf(product.category) }
    var showDeleteWarning by remember { mutableStateOf(false) }

    if (showDeleteWarning) {
        AlertDialog(
            onDismissRequest = { showDeleteWarning = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Удалить товар?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Товар «${product.name}» будет удален безвозвратно.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteWarning = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GymRoseAlert)
                ) { Text("Удалить", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWarning = false }) { Text("Отмена") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Настройки товара", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название товара") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Цена") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Количество на складе") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Категория:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    CustomDropdownFilter(
                        options = mapOf(
                            "drinks" to "Напитки",
                            "supplements" to "Спортпит",
                            "equipment" to "Инвентарь"
                        ),
                        selectedKey = category,
                        onItemSelected = { category = it },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showDeleteWarning = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GymRoseAlert)
                    ) {
                        Text("Удалить товар")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = priceStr.toDoubleOrNull() ?: product.price
                        val stock = stockStr.toIntOrNull() ?: product.stockQuantity
                        onSave(name, category, price, stock)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo)
                ) {
                    Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        )
    }
}

@Composable
fun GeneralSellProductDialog(
    product: Product,
    currencyCode: String,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onSell: (clientId: Long, quantity: Int, paymentMethod: String) -> Unit
) {
    var selectedClientId by remember { mutableLongStateOf(clients.firstOrNull()?.id ?: 1L) }
    var quantity by remember { mutableIntStateOf(1) }
    var paymentMethod by remember { mutableStateOf("cash") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredClients = clients.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) || 
        it.clientCode.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Продать: ${product.name}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Выберите покупателя:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск клиента...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(modifier = Modifier.heightIn(max = 150.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filteredClients) { c ->
                        val isSelected = selectedClientId == c.id
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { selectedClientId = c.id }
                        ) {
                            Text(
                                text = "${c.fullName} (${c.clientCode})",
                                modifier = Modifier.padding(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
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
                        IconButton(onClick = { if (quantity > 1) quantity-- }) { Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                        Text("$quantity", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { quantity++ }) { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                    }
                }

                Text("Сумма: ${(product.price * quantity).toInt()} $currencyCode", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMethod == "cash",
                        onClick = { paymentMethod = "cash" },
                        label = { Text("Наличные") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                    )
                    FilterChip(
                        selected = paymentMethod == "card",
                        onClick = { paymentMethod = "card" },
                        label = { Text("Карта") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSell(selectedClientId, quantity, paymentMethod) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Оформить продажу", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

