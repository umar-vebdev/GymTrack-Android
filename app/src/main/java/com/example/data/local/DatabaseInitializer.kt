package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DatabaseInitializer {

    suspend fun seedSampleDataIfNeeded(db: GymDatabase) = withContext(Dispatchers.IO) {
        val clientDao = db.clientDao()
        val membershipDao = db.membershipDao()
        val productDao = db.productDao()
        val saleDao = db.saleDao()
        val visitDao = db.visitDao()

        // Check if database is already populated
        val existingClients = clientDao.getClientById(1)
        if (existingClients != null) return@withContext

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val dayOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        val now = Date()
        val nowStr = dateFormat.format(now)

        fun dateOffset(days: Int): String {
            val cal = Calendar.getInstance()
            cal.time = now
            cal.add(Calendar.DAY_OF_YEAR, days)
            return dayOnlyFormat.format(cal.time)
        }

        fun dateTimeOffset(days: Int, hours: Int = 0): String {
            val cal = Calendar.getInstance()
            cal.time = now
            cal.add(Calendar.DAY_OF_YEAR, days)
            cal.add(Calendar.HOUR_OF_DAY, hours)
            return dateFormat.format(cal.time)
        }

        // 1. Seed Membership Types (Tariffs)
        val mTypes = listOf(
            MembershipTypeEntity(id = 1, name = "12 Занятий (1 Месяц)", durationType = "visits", durationValue = 12, price = 3500.0),
            MembershipTypeEntity(id = 2, name = " Безлимит 1 Месяц", durationType = "days", durationValue = 30, price = 4800.0),
            MembershipTypeEntity(id = 3, name = " Безлимит 3 Месяца", durationType = "days", durationValue = 90, price = 12500.0),
            MembershipTypeEntity(id = 4, name = "8 Занятий (Студенческий)", durationType = "visits", durationValue = 8, price = 2400.0),
            MembershipTypeEntity(id = 5, name = " Годовой Персональный", durationType = "days", durationValue = 365, price = 39000.0)
        )
        mTypes.forEach { membershipDao.insertMembershipType(it) }

        // 2. Seed Products
        val products = listOf(
            ProductEntity(id = 1, name = "Вода питьевая 0.5L", category = "drinks", price = 100.0, stockQuantity = 85),
            ProductEntity(id = 2, name = "Протеиновый батончик (Choco-Peanut)", category = "supplements", price = 180.0, stockQuantity = 42),
            ProductEntity(id = 3, name = "Изотоник Powerade 0.5L", category = "drinks", price = 160.0, stockQuantity = 30),
            ProductEntity(id = 4, name = "Шейкер GymTrack 700ml", category = "equipment", price = 650.0, stockQuantity = 15),
            ProductEntity(id = 5, name = "BCAA Порционный 10g", category = "supplements", price = 120.0, stockQuantity = 60),
            ProductEntity(id = 6, name = "Спортивное полотенце микрофибра", category = "apparel", price = 890.0, stockQuantity = 20),
            ProductEntity(id = 7, name = "Предтренировочный комплекс (шот)", category = "supplements", price = 220.0, stockQuantity = 25)
        )
        products.forEach { productDao.insertProduct(it) }

        // 3. Seed Clients
        val clients = listOf(
            ClientEntity(id = 1, clientCode = "GT-0042", fullName = "Иванов Александр Сергеевич", phone = "+7 (999) 123-45-67", note = "Предпочитает вечерние тренировки"),
            ClientEntity(id = 2, clientCode = "GT-0015", fullName = "Петрова Мария Игоревна", phone = "+7 (912) 345-67-89", note = "Занимается с тренером"),
            ClientEntity(id = 3, clientCode = "GT-0088", fullName = "Смирнов Дмитрий Алексеевич", phone = "+7 (903) 765-43-21"),
            ClientEntity(id = 4, clientCode = "GT-0104", fullName = "Кузнецова Анна Владимировна", phone = "+7 (926) 888-99-00", note = "Истекает абонемент! Позвонить"),
            ClientEntity(id = 5, clientCode = "GT-0023", fullName = "Соколов Артем Олегович", phone = "+7 (950) 111-22-33"),
            ClientEntity(id = 6, clientCode = "GT-0056", fullName = "Волкова Екатерина Николаевна", phone = "+7 (964) 444-55-66"),
            ClientEntity(id = 7, clientCode = "GT-0071", fullName = "Морозов Максим Викторович", phone = "+7 (985) 222-33-44"),
            ClientEntity(id = 8, clientCode = "GT-0092", fullName = "Васильева Елена Сергеевна", phone = "+7 (901) 333-77-88")
        )
        clients.forEach { clientDao.insertClient(it) }

        // 4. Seed Membership Purchases
        val purchases = listOf(
            // Ivanov - 12 visits tariff, 5 left
            MembershipPurchaseEntity(
                id = 1, clientId = 1, membershipTypeId = 1, amountPaid = 3500.0,
                startsAt = dateOffset(-10), expiresAt = dateOffset(20), visitsLeft = 5, totalVisits = 12, paymentMethod = "card", createdAt = dateTimeOffset(-10)
            ),
            // Petrova - 1 Month Unlimited, expires in 3 days (Expiring report target!)
            MembershipPurchaseEntity(
                id = 2, clientId = 2, membershipTypeId = 2, amountPaid = 4800.0,
                startsAt = dateOffset(-27), expiresAt = dateOffset(3), visitsLeft = null, totalVisits = null, paymentMethod = "cash", createdAt = dateTimeOffset(-27)
            ),
            // Smirnov - 3 Months Unlimited, expires in 45 days
            MembershipPurchaseEntity(
                id = 3, clientId = 3, membershipTypeId = 3, amountPaid = 12500.0,
                startsAt = dateOffset(-15), expiresAt = dateOffset(75), visitsLeft = null, totalVisits = null, paymentMethod = "card", createdAt = dateTimeOffset(-15)
            ),
            // Kuznetsova - 8 visits, only 1 left! (Expiring report target!)
            MembershipPurchaseEntity(
                id = 4, clientId = 4, membershipTypeId = 4, amountPaid = 2400.0,
                startsAt = dateOffset(-12), expiresAt = dateOffset(18), visitsLeft = 1, totalVisits = 8, paymentMethod = "cash", createdAt = dateTimeOffset(-12)
            ),
            // Sokolov - 1 Month Unlimited, expires in 5 days
            MembershipPurchaseEntity(
                id = 5, clientId = 5, membershipTypeId = 2, amountPaid = 4800.0,
                startsAt = dateOffset(-25), expiresAt = dateOffset(5), visitsLeft = null, totalVisits = null, paymentMethod = "card", createdAt = dateTimeOffset(-25)
            ),
            // Morozov - 12 visits, 10 left
            MembershipPurchaseEntity(
                id = 6, clientId = 7, membershipTypeId = 1, amountPaid = 3500.0,
                startsAt = dateOffset(-2), expiresAt = dateOffset(28), visitsLeft = 10, totalVisits = 12, paymentMethod = "card", createdAt = dateTimeOffset(-2)
            )
        )
        purchases.forEach { membershipDao.insertPurchase(it) }

        // 5. Seed Visits (including today's visits for active dashboard)
        val visits = listOf(
            VisitEntity(id = 1, clientId = 1, membershipPurchaseId = 1, visitedAt = dateTimeOffset(0, -2)),
            VisitEntity(id = 2, clientId = 3, membershipPurchaseId = 3, visitedAt = dateTimeOffset(0, -4)),
            VisitEntity(id = 3, clientId = 2, membershipPurchaseId = 2, visitedAt = dateTimeOffset(0, -5)),
            VisitEntity(id = 4, clientId = 5, membershipPurchaseId = 5, visitedAt = dateTimeOffset(-1, -3)),
            VisitEntity(id = 5, clientId = 1, membershipPurchaseId = 1, visitedAt = dateTimeOffset(-2, -1)),
            VisitEntity(id = 6, clientId = 4, membershipPurchaseId = 4, visitedAt = dateTimeOffset(-3, -2)),
            VisitEntity(id = 7, clientId = 7, membershipPurchaseId = 6, visitedAt = dateTimeOffset(0, -1))
        )
        visits.forEach { visitDao.insertVisit(it) }

        // 6. Seed Product Sales
        val sales = listOf(
            ProductSaleEntity(id = 1, clientId = 1, productId = 1, quantity = 2, totalPrice = 200.0, paymentMethod = "cash", createdAt = dateTimeOffset(0, -2)),
            ProductSaleEntity(id = 2, clientId = 1, productId = 2, quantity = 1, totalPrice = 180.0, paymentMethod = "cash", createdAt = dateTimeOffset(0, -2)),
            ProductSaleEntity(id = 3, clientId = 3, productId = 3, quantity = 1, totalPrice = 160.0, paymentMethod = "card", createdAt = dateTimeOffset(0, -4)),
            ProductSaleEntity(id = 4, clientId = 2, productId = 1, quantity = 1, totalPrice = 100.0, paymentMethod = "cash", createdAt = dateTimeOffset(0, -5)),
            ProductSaleEntity(id = 5, clientId = 7, productId = 4, quantity = 1, totalPrice = 650.0, paymentMethod = "card", createdAt = dateTimeOffset(0, -1)),
            ProductSaleEntity(id = 6, clientId = 5, productId = 5, quantity = 2, totalPrice = 240.0, paymentMethod = "card", createdAt = dateTimeOffset(-1, -3))
        )
        sales.forEach { saleDao.insertSale(it) }
    }
}
