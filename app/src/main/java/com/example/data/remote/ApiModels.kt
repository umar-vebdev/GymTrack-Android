package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "meta") val meta: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "code") val code: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "details") val details: Map<String, List<String>>? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "token") val token: String,
    @Json(name = "user") val user: UserDto
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "role") val role: String? = "staff"
)

@JsonClass(generateAdapter = true)
data class ClientDto(
    @Json(name = "id") val id: Long,
    @Json(name = "client_code") val clientCode: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "membership_purchases") val membershipPurchases: List<MembershipPurchaseDto>? = null,
    @Json(name = "visits") val visits: List<VisitDto>? = null,
    @Json(name = "product_sales") val productSales: List<ProductSaleDto>? = null
)

@JsonClass(generateAdapter = true)
data class CreateClientRequest(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone") val phone: String
)

@JsonClass(generateAdapter = true)
data class MembershipTypeDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "duration_type") val durationType: String,
    @Json(name = "duration_value") val durationValue: Int,
    @Json(name = "price") val price: Double,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class MembershipPurchaseDto(
    @Json(name = "id") val id: Long,
    @Json(name = "client_id") val clientId: Long,
    @Json(name = "membership_type_id") val membershipTypeId: Long,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "starts_at") val startsAt: String,
    @Json(name = "expires_at") val expiresAt: String? = null,
    @Json(name = "visits_left") val visitsLeft: Int? = null,
    @Json(name = "payment_method") val paymentMethod: String = "card",
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CreatePurchaseRequest(
    @Json(name = "client_id") val clientId: Long,
    @Json(name = "membership_type_id") val membershipTypeId: Long,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "starts_at") val startsAt: String,
    @Json(name = "expires_at") val expiresAt: String? = null,
    @Json(name = "visits_left") val visitsLeft: Int? = null,
    @Json(name = "payment_method") val paymentMethod: String
)

@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: Double,
    @Json(name = "category") val category: String,
    @Json(name = "is_active") val isActive: Boolean = true,
    @Json(name = "stock_quantity") val stockQuantity: Int = 100
)

@JsonClass(generateAdapter = true)
data class CreateProductRequest(
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: Double,
    @Json(name = "category") val category: String,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ProductSaleDto(
    @Json(name = "id") val id: Long,
    @Json(name = "client_id") val clientId: Long,
    @Json(name = "product_id") val productId: Long,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "total_price") val totalPrice: Double? = null,
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "client") val client: ClientDto? = null,
    @Json(name = "product") val product: ProductDto? = null
)

@JsonClass(generateAdapter = true)
data class CreateSaleRequest(
    @Json(name = "client_id") val clientId: Long,
    @Json(name = "product_id") val productId: Long,
    @Json(name = "quantity") val quantity: Int = 1,
    @Json(name = "payment_method") val paymentMethod: String = "cash"
)

@JsonClass(generateAdapter = true)
data class VisitDto(
    @Json(name = "id") val id: Long,
    @Json(name = "client_id") val clientId: Long,
    @Json(name = "membership_purchase_id") val membershipPurchaseId: Long,
    @Json(name = "visited_at") val visitedAt: String,
    @Json(name = "client") val client: ClientDto? = null
)

@JsonClass(generateAdapter = true)
data class DashboardReportDto(
    @Json(name = "today_revenue") val todayRevenue: Double,
    @Json(name = "today_visits_count") val todayVisitsCount: Int,
    @Json(name = "today_sales_count") val todaySalesCount: Int,
    @Json(name = "active_memberships") val activeMemberships: Int
)

@JsonClass(generateAdapter = true)
data class HistoryItemDto(
    @Json(name = "type") val type: String, // "visit", "product_sale", "membership_purchase"
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "data") val data: Map<String, Any>? = null
)
