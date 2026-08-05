package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientCode: String, // e.g. GT-0042
    val fullName: String,
    val phone: String,
    val photoUrl: String? = null,
    val note: String? = null,
    val isActive: Boolean = true,
    val createdAt: String = System.currentTimeMillis().toString()
)

@Entity(tableName = "membership_types")
data class MembershipTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "12 Занятий", "1 Месяц Безлимит"
    val durationType: String, // "visits" or "days"
    val durationValue: Int, // e.g. 12 (visits) or 30 (days)
    val price: Double,
    val isActive: Boolean = true
)

@Entity(tableName = "membership_purchases")
data class MembershipPurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val membershipTypeId: Long,
    val amountPaid: Double,
    val startsAt: String, // YYYY-MM-DD or ISO
    val expiresAt: String? = null, // YYYY-MM-DD
    val visitsLeft: Int? = null,
    val totalVisits: Int? = null,
    val paymentMethod: String = "card", // "cash" or "card"
    val createdAt: String
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "drinks", "supplements", "apparel", "other"
    val price: Double,
    val stockQuantity: Int = 100,
    val isActive: Boolean = true
)

@Entity(tableName = "product_sales")
data class ProductSaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val productId: Long,
    val quantity: Int = 1,
    val totalPrice: Double,
    val paymentMethod: String = "cash", // "cash" or "card"
    val createdAt: String
)

@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val membershipPurchaseId: Long,
    val visitedAt: String
)
