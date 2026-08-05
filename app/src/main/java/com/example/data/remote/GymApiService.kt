package com.example.data.remote

import retrofit2.Response
import retrofit2.http.*

interface GymApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    // Clients
    @GET("clients")
    suspend fun getClients(
        @Query("search") search: String? = null,
        @Query("membership_type_id") membershipTypeId: Long? = null,
        @Query("per_page") perPage: Int = 50
    ): Response<ApiResponse<List<ClientDto>>>

    @POST("clients")
    suspend fun createClient(@Body request: CreateClientRequest): Response<ApiResponse<ClientDto>>

    @GET("clients/{id}")
    suspend fun getClientDetail(@Path("id") id: Long): Response<ApiResponse<ClientDto>>

    @PUT("clients/{id}")
    suspend fun updateClient(
        @Path("id") id: Long,
        @Body request: CreateClientRequest
    ): Response<ApiResponse<ClientDto>>

    @POST("clients/{id}/deduct-visit")
    suspend fun deductVisit(@Path("id") id: Long): Response<ApiResponse<VisitDto>>

    // Membership Types & Purchases
    @GET("membership-types")
    suspend fun getMembershipTypes(@Query("all") all: Boolean = false): Response<ApiResponse<List<MembershipTypeDto>>>

    @POST("membership-purchases")
    suspend fun purchaseMembership(@Body request: CreatePurchaseRequest): Response<ApiResponse<MembershipPurchaseDto>>

    // Products & Sales
    @GET("products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("all") all: Boolean = false
    ): Response<ApiResponse<List<ProductDto>>>

    @POST("products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<ApiResponse<ProductDto>>

    @POST("product-sales")
    suspend fun sellProduct(@Body request: CreateSaleRequest): Response<ApiResponse<ProductSaleDto>>

    // Reports
    @GET("reports/dashboard")
    suspend fun getDashboardReport(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<ApiResponse<DashboardReportDto>>

    @GET("reports/history")
    suspend fun getHistoryReport(
        @Query("type") type: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): Response<ApiResponse<Map<String, Any>>>

    @GET("reports/expiring-memberships")
    suspend fun getExpiringMemberships(@Query("days") days: Int = 7): Response<ApiResponse<List<ClientDto>>>
}
