package com.example.data.repository

import com.example.data.local.*
import com.example.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GymRepository(private val db: GymDatabase) {

    private val clientDao = db.clientDao()
    private val membershipDao = db.membershipDao()
    private val productDao = db.productDao()
    private val saleDao = db.saleDao()
    private val visitDao = db.visitDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Ensure sample data is initialized
    suspend fun ensureDataInitialized() {
        DatabaseInitializer.seedSampleDataIfNeeded(db)
    }

    // 1. CLIENTS
    fun searchClients(query: String): Flow<List<Client>> {
        val rawClientsFlow = if (query.isBlank()) {
            clientDao.getAllClients()
        } else {
            clientDao.searchClients(query.trim())
        }

        return combine(rawClientsFlow, membershipDao.getAllPurchases(), membershipDao.getAllMembershipTypes()) { clients, purchases, types ->
            val typeMap = types.associateBy { it.id }
            clients.map { client ->
                val activePurchase = purchases
                    .filter { it.clientId == client.id }
                    .maxByOrNull { it.createdAt }

                val summary = activePurchase?.let { p ->
                    val type = typeMap[p.membershipTypeId]
                    val isExpired = p.expiresAt?.let { isDateBeforeToday(it) } ?: false
                    MembershipPurchaseSummary(
                        purchaseId = p.id,
                        tariffName = type?.name ?: "Абонемент",
                        durationType = type?.durationType ?: "visits",
                        visitsLeft = p.visitsLeft,
                        totalVisits = p.totalVisits,
                        expiresAt = p.expiresAt,
                        startsAt = p.startsAt,
                        isExpired = isExpired || (p.visitsLeft != null && p.visitsLeft <= 0)
                    )
                }

                Client(
                    id = client.id,
                    clientCode = client.clientCode,
                    fullName = client.fullName,
                    phone = client.phone,
                    photoUrl = client.photoUrl,
                    note = client.note,
                    isActive = client.isActive,
                    activeMembership = summary
                )
            }
        }
    }

    fun getClientByIdFlow(id: Long): Flow<Client?> {
        return combine(clientDao.getClientByIdFlow(id), membershipDao.getPurchasesForClient(id), membershipDao.getAllMembershipTypes()) { client, purchases, types ->
            if (client == null) return@combine null
            val typeMap = types.associateBy { it.id }
            val activePurchase = purchases.maxByOrNull { it.createdAt }

            val summary = activePurchase?.let { p ->
                val type = typeMap[p.membershipTypeId]
                val isExpired = p.expiresAt?.let { isDateBeforeToday(it) } ?: false
                MembershipPurchaseSummary(
                    purchaseId = p.id,
                    tariffName = type?.name ?: "Абонемент",
                    durationType = type?.durationType ?: "visits",
                    visitsLeft = p.visitsLeft,
                    totalVisits = p.totalVisits,
                    expiresAt = p.expiresAt,
                    startsAt = p.startsAt,
                    isExpired = isExpired || (p.visitsLeft != null && p.visitsLeft <= 0)
                )
            }

            Client(
                id = client.id,
                clientCode = client.clientCode,
                fullName = client.fullName,
                phone = client.phone,
                photoUrl = client.photoUrl,
                note = client.note,
                isActive = client.isActive,
                activeMembership = summary
            )
        }
    }

    suspend fun addClient(fullName: String, phone: String, note: String?): Result<ClientEntity> = withContext(Dispatchers.IO) {
        try {
            val codeNumber = (1000..9999).random()
            val clientCode = "GT-$codeNumber"
            val entity = ClientEntity(
                clientCode = clientCode,
                fullName = fullName.trim(),
                phone = phone.trim(),
                note = note?.trim()
            )
            val newId = clientDao.insertClient(entity)
            Result.success(entity.copy(id = newId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteClient(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            clientDao.deleteClient(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. CHECK-IN / DEDUCT VISIT (-1 Посещение)
    suspend fun deductVisit(clientId: Long): Result<String> = withContext(Dispatchers.IO) {
        try {
            clientDao.getClientById(clientId) ?: return@withContext Result.failure(Exception("Клиент не найден"))
            val purchases = membershipDao.getPurchasesForClientSync(clientId)
            if (purchases.isEmpty()) {
                return@withContext Result.failure(Exception("У клиента нет купленных абонементов!"))
            }

            // Find valid active purchase
            val validPurchase = purchases.firstOrNull { p ->
                val notExpiredDate = p.expiresAt?.let { !isDateBeforeToday(it) } ?: true
                val hasVisits = p.visitsLeft == null || p.visitsLeft > 0
                notExpiredDate && hasVisits
            } ?: return@withContext Result.failure(Exception("Активный абонемент истек или закончились визиты!"))

            val nowStr = dateFormat.format(Date())

            if (validPurchase.visitsLeft != null) {
                // Deduct 1 visit
                val newVisitsLeft = validPurchase.visitsLeft - 1
                membershipDao.updatePurchase(validPurchase.copy(visitsLeft = newVisitsLeft))
                visitDao.insertVisit(VisitEntity(clientId = clientId, membershipPurchaseId = validPurchase.id, visitedAt = nowStr))
                Result.success("Успешная отметка! Списано 1 посещение. Осталось: $newVisitsLeft")
            } else {
                // Unlimited days membership
                visitDao.insertVisit(VisitEntity(clientId = clientId, membershipPurchaseId = validPurchase.id, visitedAt = nowStr))
                Result.success("Успешная отметка! Безлимитный визит зафиксирован.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelCheckIn(visitId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val visit = visitDao.getVisitById(visitId) ?: return@withContext Result.failure(Exception("Визит не найден"))
            val purchase = membershipDao.getPurchaseById(visit.membershipPurchaseId)
            
            if (purchase != null && purchase.visitsLeft != null) {
                // Return 1 visit
                membershipDao.updatePurchase(purchase.copy(visitsLeft = purchase.visitsLeft + 1))
            }
            
            visitDao.deleteVisit(visitId)
            Result.success(Unit)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }

    // 3. MEMBERSHIPS & PURCHASES
    fun getMembershipTypes(): Flow<List<MembershipType>> {
        return membershipDao.getAllMembershipTypes().map { list ->
            list.map {
                MembershipType(
                    id = it.id,
                    name = it.name,
                    durationType = it.durationType,
                    durationValue = it.durationValue,
                    price = it.price,
                    isActive = it.isActive
                )
            }
        }
    }

    fun getMembershipTypesAdmin(): Flow<List<MembershipType>> {
        return membershipDao.getAllMembershipTypesAdmin().map { list ->
            list.map {
                MembershipType(
                    id = it.id,
                    name = it.name,
                    durationType = it.durationType,
                    durationValue = it.durationValue,
                    price = it.price,
                    isActive = it.isActive
                )
            }
        }
    }

    suspend fun addMembershipType(name: String, durationType: String, durationValue: Int, price: Double): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            membershipDao.insertMembershipType(MembershipTypeEntity(
                name = name, durationType = durationType, durationValue = durationValue, price = price
            ))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteMembershipType(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            membershipDao.deleteMembershipType(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMembershipType(id: Long, name: String, durationType: String, durationValue: Int, price: Double): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val m = membershipDao.getMembershipTypeById(id) ?: return@withContext Result.failure(Exception("Тариф не найден"))
            membershipDao.updateMembershipType(m.copy(
                name = name, durationType = durationType, durationValue = durationValue, price = price
            ))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun purchaseMembership(
        clientId: Long,
        membershipTypeId: Long,
        paymentMethod: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val type = membershipDao.getMembershipTypeById(membershipTypeId) ?: return@withContext Result.failure(Exception("Тариф не найден"))
            
            val now = Date()
            val startsAt = dayFormat.format(now)
            val cal = Calendar.getInstance()
            cal.time = now

            var expiresAt: String? = null
            var visitsLeft: Int? = null
            var totalVisits: Int? = null

            if (type.durationType == "visits") {
                visitsLeft = type.durationValue
                totalVisits = type.durationValue
                cal.add(Calendar.DAY_OF_YEAR, 60) // Visits pass valid for 60 days default
                expiresAt = dayFormat.format(cal.time)
            } else {
                cal.add(Calendar.DAY_OF_YEAR, type.durationValue)
                expiresAt = dayFormat.format(cal.time)
            }

            val purchase = MembershipPurchaseEntity(
                clientId = clientId,
                membershipTypeId = membershipTypeId,
                amountPaid = type.price,
                startsAt = startsAt,
                expiresAt = expiresAt,
                visitsLeft = visitsLeft,
                totalVisits = totalVisits,
                paymentMethod = paymentMethod,
                createdAt = dateFormat.format(now)
            )
            membershipDao.insertPurchase(purchase)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getClientPurchases(clientId: Long): Flow<List<MembershipPurchase>> {
        return combine(membershipDao.getPurchasesForClient(clientId), membershipDao.getAllMembershipTypesAdmin()) { purchases, types ->
            val typeMap = types.associateBy { it.id }
            purchases.map { p ->
                val t = typeMap[p.membershipTypeId]
                MembershipPurchase(
                    id = p.id,
                    clientId = p.clientId,
                    membershipTypeId = p.membershipTypeId,
                    membershipTypeName = t?.name ?: "Абонемент",
                    amountPaid = p.amountPaid,
                    startsAt = p.startsAt,
                    expiresAt = p.expiresAt,
                    visitsLeft = p.visitsLeft,
                    totalVisits = p.totalVisits,
                    paymentMethod = p.paymentMethod,
                    createdAt = p.createdAt
                )
            }
        }
    }

    // 4. PRODUCTS & SALES
    fun getProducts(category: String? = null, query: String = ""): Flow<List<Product>> {
        val flow = if (query.isBlank()) productDao.getAllProducts() else productDao.searchProducts(query)
        return flow.map { list ->
            list.filter { category == null || category == "all" || it.category.equals(category, ignoreCase = true) }
                .map {
                    Product(
                        id = it.id,
                        name = it.name,
                        category = it.category,
                        price = it.price,
                        stockQuantity = it.stockQuantity,
                        isActive = it.isActive
                    )
                }
        }
    }

    fun getProductsAdmin(categories: Set<String> = emptySet(), query: String = ""): Flow<List<Product>> {
        val flow = if (query.isBlank()) productDao.getAllProductsAdmin() else productDao.searchProductsAdmin(query)
        return flow.map { list ->
            list.filter { 
                categories.isEmpty() || 
                categories.contains("all") || 
                (categories.contains("inactive") && !it.isActive) ||
                categories.contains(it.category.lowercase()) 
            }.map {
                    Product(
                        id = it.id,
                        name = it.name,
                        category = it.category,
                        price = it.price,
                        stockQuantity = it.stockQuantity,
                        isActive = it.isActive
                    )
                }
        }
    }

    suspend fun addProduct(name: String, category: String, price: Double, stock: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val product = ProductEntity(
                name = name.trim(),
                category = category,
                price = price,
                stockQuantity = stock
            )
            productDao.insertProduct(product)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(id: Long, name: String, category: String, price: Double, stock: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val p = productDao.getProductById(id) ?: return@withContext Result.failure(Exception("Товар не найден"))
            productDao.updateProduct(p.copy(name = name.trim(), category = category, price = price, stockQuantity = stock))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            productDao.deleteProduct(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sellProduct(clientId: Long, productId: Long, quantity: Int, paymentMethod: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val product = productDao.getProductById(productId) ?: return@withContext Result.failure(Exception("Товар не найден"))
            if (!product.isActive) return@withContext Result.failure(Exception("Товар неактивен"))
            val total = product.price * quantity

            val sale = ProductSaleEntity(
                clientId = clientId,
                productId = productId,
                quantity = quantity,
                totalPrice = total,
                paymentMethod = paymentMethod,
                createdAt = dateFormat.format(Date())
            )
            saleDao.insertSale(sale)

            // Reduce stock
            val newStock = (product.stockQuantity - quantity).coerceAtLeast(0)
            productDao.updateProduct(product.copy(stockQuantity = newStock))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getClientSales(clientId: Long): Flow<List<ProductSale>> {
        return combine(saleDao.getSalesForClient(clientId), productDao.getAllProducts(), clientDao.getAllClients()) { sales, products, clients ->
            val prodMap = products.associateBy { it.id }
            val clientMap = clients.associateBy { it.id }
            sales.map { s ->
                val p = prodMap[s.productId]
                val c = clientMap[s.clientId]
                ProductSale(
                    id = s.id,
                    clientId = s.clientId,
                    clientName = c?.fullName ?: "Клиент #${s.clientId}",
                    productId = s.productId,
                    productName = p?.name ?: "Товар",
                    quantity = s.quantity,
                    totalPrice = s.totalPrice,
                    paymentMethod = s.paymentMethod,
                    createdAt = s.createdAt
                )
            }
        }
    }

    fun getClientVisits(clientId: Long): Flow<List<Visit>> {
        return combine(visitDao.getVisitsForClient(clientId), clientDao.getAllClients()) { visits, clients ->
            val clientMap = clients.associateBy { it.id }
            visits.map { v ->
                val c = clientMap[v.clientId]
                Visit(
                    id = v.id,
                    clientId = v.clientId,
                    clientName = c?.fullName ?: "Клиент",
                    visitedAt = v.visitedAt
                )
            }
        }
    }

    // 5. GLOBAL JOURNAL (POLYMORPHIC HISTORY STREAM)
    fun getHistoryEvents(filterTypes: Set<String> = emptySet(), dateFilter: String = "all"): Flow<List<HistoryEvent>> {
        val todayStr = dayFormat.format(Date())

        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dayFormat.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        val weekAgoStr = dayFormat.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        val monthAgoStr = dayFormat.format(cal.time)
        val flow1 = combine(visitDao.getAllVisits(), saleDao.getAllSales(), membershipDao.getAllPurchases()) { visits, sales, purchases ->
            Triple(visits, sales, purchases)
        }
        val flow2 = combine(clientDao.getAllClients(), productDao.getAllProductsAdmin(), membershipDao.getAllMembershipTypesAdmin()) { clients, products, mTypes ->
            Triple(clients, products, mTypes)
        }

        return combine(flow1, flow2) { (visits, sales, purchases), (clients, products, mTypes) ->
            val clientMap = clients.associateBy { it.id }
            val productMap = products.associateBy { it.id }
            val typeMap = mTypes.associateBy { it.id }

            val events = mutableListOf<HistoryEvent>()
            
            val isAll = filterTypes.isEmpty() || filterTypes.contains("all")

            if (isAll || filterTypes.contains("visit")) {
                visits.forEach { v ->
                    val c = clientMap[v.clientId]
                    events.add(
                        HistoryEvent.VisitEvent(
                            id = "visit_${v.id}",
                            title = "Посещение зала",
                            description = "Очно в зале",
                            clientId = c?.id ?: v.clientId,
                            clientName = c?.fullName ?: "Клиент #${v.clientId}",
                            clientCode = c?.clientCode ?: "",
                            timestamp = v.visitedAt
                        )
                    )
                }
            }

            if (isAll || filterTypes.contains("product_sale")) {
                sales.forEach { s ->
                    val c = clientMap[s.clientId]
                    val p = productMap[s.productId]
                    events.add(
                        HistoryEvent.SaleEvent(
                            id = "sale_${s.id}",
                            title = " Продажа товара: ${p?.name ?: "Товар"}",
                            description = "Количество: ${s.quantity} шт. | Способ: ${if (s.paymentMethod == "cash") "Наличные" else "Карта"}",
                            clientId = c?.id ?: s.clientId,
                            clientName = c?.fullName ?: "Клиент #${s.clientId}",
                            clientCode = c?.clientCode ?: "",
                            timestamp = s.createdAt,
                            amount = s.totalPrice,
                            paymentMethod = s.paymentMethod
                        )
                    )
                }
            }

            if (isAll || filterTypes.contains("membership_purchase")) {
                purchases.forEach { mp ->
                    val c = clientMap[mp.clientId]
                    val t = typeMap[mp.membershipTypeId]
                    events.add(
                        HistoryEvent.MembershipEvent(
                            id = "purchase_${mp.id}",
                            title = " Продажа абонемента: ${t?.name ?: "Абонемент"}",
                            description = "Действует с ${mp.startsAt} | Способ: ${if (mp.paymentMethod == "cash") "Наличные" else "Карта"}",
                            clientId = c?.id ?: mp.clientId,
                            clientName = c?.fullName ?: "Клиент #${mp.clientId}",
                            clientCode = c?.clientCode ?: "",
                            timestamp = mp.createdAt,
                            amount = mp.amountPaid,
                            paymentMethod = mp.paymentMethod
                        )
                    )
                }
            }

            val filterDatePredicate: (String) -> Boolean = { dateStrText ->
                val dateStr = if (dateStrText.length >= 10) dateStrText.substring(0, 10) else dateStrText
                if (dateFilter.startsWith("custom|")) {
                    val parts = dateFilter.split("|")
                    if (parts.size == 3) {
                        val start = parts[1]
                        val end = parts[2]
                        if (start.isNotBlank() && end.isNotBlank()) {
                            dateStr in start..end
                        } else true
                    } else true
                } else {
                    when (dateFilter) {
                        "yesterday" -> dateStr == yesterdayStr
                        "week" -> dateStr >= weekAgoStr
                        "month" -> dateStr >= monthAgoStr
                        "all" -> true
                        else -> dateStr == todayStr
                    }
                }
            }

            events.filter { filterDatePredicate(it.timestamp) }
                .sortedByDescending { it.timestamp }
        }
    }

    // 6. DASHBOARD & ANALYTICS WITH DATE RANGE FILTER
    fun getDashboardStatsFiltered(rangeType: String = "today"): Flow<DashboardStats> {
        val todayStr = dayFormat.format(Date())

        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dayFormat.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        val weekAgoStr = dayFormat.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        val monthAgoStr = dayFormat.format(cal.time)

        return combine(
            visitDao.getAllVisits(),
            saleDao.getAllSales(),
            membershipDao.getAllPurchases()
        ) { visits, sales, purchases ->
            val filterDatePredicate: (String) -> Boolean = { dateStrText ->
                val dateStr = if (dateStrText.length >= 10) dateStrText.substring(0, 10) else dateStrText
                if (rangeType.startsWith("custom|")) {
                    val parts = rangeType.split("|")
                    if (parts.size == 3) {
                        val start = parts[1]
                        val end = parts[2]
                        if (start.isNotBlank() && end.isNotBlank()) {
                            dateStr in start..end
                        } else true
                    } else true
                } else {
                    when (rangeType) {
                        "yesterday" -> dateStr == yesterdayStr
                        "week" -> dateStr >= weekAgoStr
                        "month" -> dateStr >= monthAgoStr
                        "all" -> true
                        else -> dateStr == todayStr
                    }
                }
            }

            val filteredVisits = visits.count { filterDatePredicate(it.visitedAt) }

            val filteredSales = sales.filter { filterDatePredicate(it.createdAt) }
            val salesRevenue = filteredSales.sumOf { it.totalPrice }

            val filteredPurchases = purchases.filter { filterDatePredicate(it.createdAt) }
            val purchasesRevenue = filteredPurchases.sumOf { it.amountPaid }

            val totalRevenue = salesRevenue + purchasesRevenue

            val cashRevenue = filteredSales.filter { it.paymentMethod == "cash" }.sumOf { it.totalPrice } +
                    filteredPurchases.filter { it.paymentMethod == "cash" }.sumOf { it.amountPaid }

            val cardRevenue = filteredSales.filter { it.paymentMethod == "card" }.sumOf { it.totalPrice } +
                    filteredPurchases.filter { it.paymentMethod == "card" }.sumOf { it.amountPaid }

            DashboardStats(
                todayRevenue = totalRevenue,
                todayVisits = filteredVisits,
                todaySalesCount = filteredSales.size,
                activeMemberships = purchases.size,
                cashRevenue = cashRevenue,
                cardRevenue = cardRevenue
            )
        }
    }

    // 7. EXPIRING MEMBERSHIPS REPORT (Истекающие абонементы)
    fun getExpiringMemberships(daysThreshold: Int = 7): Flow<List<ExpiringMembershipInfo>> {
        return combine(
            clientDao.getAllClients(),
            membershipDao.getAllPurchases(),
            membershipDao.getAllMembershipTypes()
        ) { clients, purchases, types ->
            val clientMap = clients.associateBy { it.id }
            val typeMap = types.associateBy { it.id }

            val result = mutableListOf<ExpiringMembershipInfo>()

            purchases.forEach { p ->
                val c = clientMap[p.clientId] ?: return@forEach
                val t = typeMap[p.membershipTypeId] ?: return@forEach

                var isExpiring = false
                var daysLeft: Int? = null

                if (p.expiresAt != null) {
                    daysLeft = calculateDaysUntil(p.expiresAt)
                    if (daysLeft in 0..daysThreshold) {
                        isExpiring = true
                    }
                }

                if (p.visitsLeft != null && p.visitsLeft in 1..2) {
                    isExpiring = true
                }

                if (isExpiring) {
                    result.add(
                        ExpiringMembershipInfo(
                            clientId = c.id,
                            clientCode = c.clientCode,
                            clientName = c.fullName,
                            phone = c.phone,
                            tariffName = t.name,
                            visitsLeft = p.visitsLeft,
                            expiresAt = p.expiresAt,
                            daysLeft = daysLeft
                        )
                    )
                }
            }

            result.sortedBy { it.daysLeft ?: 0 }
        }
    }

    // Helper functions
    private fun isDateBeforeToday(dateStr: String): Boolean {
        return try {
            val date = dayFormat.parse(dateStr) ?: return false
            val today = dayFormat.parse(dayFormat.format(Date())) ?: return false
            date.before(today)
        } catch (e: Exception) {
            false
        }
    }

    // 7. CURRENCY
    val currencyDao = db.currencyDao()

    fun getAllCurrencies(): Flow<List<Currency>> = currencyDao.getAllCurrencies().map { list ->
        list.map { Currency(it.id, it.name, it.code, it.isSelected) }
    }

    fun getSelectedCurrency(): Flow<Currency?> = currencyDao.getSelectedCurrency().map { it?.let { Currency(it.id, it.name, it.code, it.isSelected) } }

    suspend fun addCurrency(name: String, code: String) {
        withContext(Dispatchers.IO) {
            currencyDao.insertCurrency(CurrencyEntity(name = name, code = code))
        }
    }

    suspend fun selectCurrency(id: Long) {
        withContext(Dispatchers.IO) {
            currencyDao.clearSelection()
            currencyDao.selectCurrency(id)
        }
    }

    suspend fun deleteCurrency(id: Long, name: String, code: String, isSelected: Boolean) {
        withContext(Dispatchers.IO) {
            currencyDao.deleteCurrency(CurrencyEntity(id, name, code, isSelected))
        }
    }

    private fun calculateDaysUntil(targetDateStr: String): Int {
        return try {
            val target = dayFormat.parse(targetDateStr) ?: return 999
            val today = dayFormat.parse(dayFormat.format(Date())) ?: return 999
            val diffMs = target.time - today.time
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            999
        }
    }

    private fun formatTimeOnly(isoStr: String): String {
        return try {
            if (isoStr.length >= 16 && isoStr.contains("T")) {
                isoStr.substring(11, 16)
            } else {
                isoStr
            }
        } catch (e: Exception) {
            isoStr
        }
    }
}
