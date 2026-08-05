package com.example.domain.model

data class Client(
    val id: Long,
    val clientCode: String,
    val fullName: String,
    val phone: String,
    val photoUrl: String? = null,
    val note: String? = null,
    val isActive: Boolean = true,
    val activeMembership: MembershipPurchaseSummary? = null
)

data class MembershipPurchaseSummary(
    val purchaseId: Long,
    val tariffName: String,
    val durationType: String, // "visits" or "days"
    val visitsLeft: Int?,
    val totalVisits: Int?,
    val expiresAt: String?,
    val startsAt: String,
    val isExpired: Boolean
)

data class MembershipType(
    val id: Long,
    val name: String,
    val durationType: String, // "visits" or "days"
    val durationValue: Int,
    val price: Double
)

data class MembershipPurchase(
    val id: Long,
    val clientId: Long,
    val membershipTypeId: Long,
    val membershipTypeName: String,
    val amountPaid: Double,
    val startsAt: String,
    val expiresAt: String?,
    val visitsLeft: Int?,
    val totalVisits: Int?,
    val paymentMethod: String,
    val createdAt: String
)

data class Product(
    val id: Long,
    val name: String,
    val category: String,
    val price: Double,
    val stockQuantity: Int,
    val isActive: Boolean = true
)

data class ProductSale(
    val id: Long,
    val clientId: Long,
    val clientName: String,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double,
    val paymentMethod: String,
    val createdAt: String
)

data class Visit(
    val id: Long,
    val clientId: Long,
    val clientName: String,
    val visitedAt: String
)

sealed class HistoryEvent(
    open val id: String,
    open val type: String, // "visit", "product_sale", "membership_purchase"
    open val title: String,
    open val description: String,
    open val clientName: String,
    open val timestamp: String,
    open val amount: Double? = null,
    open val paymentMethod: String? = null
) {
    data class VisitEvent(
        override val id: String,
        override val title: String,
        override val description: String,
        override val clientName: String,
        override val timestamp: String
    ) : HistoryEvent(id, "visit", title, description, clientName, timestamp)

    data class SaleEvent(
        override val id: String,
        override val title: String,
        override val description: String,
        override val clientName: String,
        override val timestamp: String,
        override val amount: Double,
        override val paymentMethod: String
    ) : HistoryEvent(id, "product_sale", title, description, clientName, timestamp, amount, paymentMethod)

    data class MembershipEvent(
        override val id: String,
        override val title: String,
        override val description: String,
        override val clientName: String,
        override val timestamp: String,
        override val amount: Double,
        override val paymentMethod: String
    ) : HistoryEvent(id, "membership_purchase", title, description, clientName, timestamp, amount, paymentMethod)
}

data class DashboardStats(
    val todayRevenue: Double,
    val todayVisits: Int,
    val todaySalesCount: Int,
    val activeMemberships: Int,
    val cashRevenue: Double,
    val cardRevenue: Double
)

data class ExpiringMembershipInfo(
    val clientId: Long,
    val clientCode: String,
    val clientName: String,
    val phone: String,
    val tariffName: String,
    val visitsLeft: Int?,
    val expiresAt: String?,
    val daysLeft: Int?
)
