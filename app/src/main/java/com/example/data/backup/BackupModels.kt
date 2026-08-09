package com.example.data.backup

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GymBackup(
    val version: Int = 1,
    val exportedAt: String,
    val clients: List<ClientBackup>,
    val membershipTypes: List<MembershipTypeBackup>,
    val membershipPurchases: List<MembershipPurchaseBackup>,
    val products: List<ProductBackup>,
    val productSales: List<ProductSaleBackup>,
    val visits: List<VisitBackup>,
    val currencies: List<CurrencyBackup>
)

@JsonClass(generateAdapter = true)
data class ClientBackup(
    val id: Long,
    val clientCode: String,
    val fullName: String,
    val phone: String,
    val photoUrl: String?,
    val note: String?,
    val isActive: Boolean,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class MembershipTypeBackup(
    val id: Long,
    val name: String,
    val durationType: String,
    val durationValue: Int,
    val price: Double,
    val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class MembershipPurchaseBackup(
    val id: Long,
    val clientId: Long,
    val membershipTypeId: Long,
    val amountPaid: Double,
    val startsAt: String,
    val expiresAt: String?,
    val visitsLeft: Int?,
    val totalVisits: Int?,
    val paymentMethod: String,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class ProductBackup(
    val id: Long,
    val name: String,
    val category: String,
    val price: Double,
    val stockQuantity: Int,
    val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class ProductSaleBackup(
    val id: Long,
    val clientId: Long,
    val productId: Long,
    val quantity: Int,
    val totalPrice: Double,
    val paymentMethod: String,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class VisitBackup(
    val id: Long,
    val clientId: Long,
    val membershipPurchaseId: Long,
    val visitedAt: String
)

@JsonClass(generateAdapter = true)
data class CurrencyBackup(
    val id: Long,
    val name: String,
    val code: String,
    val isSelected: Boolean
)
