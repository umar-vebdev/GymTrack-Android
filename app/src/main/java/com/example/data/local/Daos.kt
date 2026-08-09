package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients WHERE isActive = 1 ORDER BY fullName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("""
        SELECT * FROM clients 
        WHERE isActive = 1 
        AND (fullName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR clientCode LIKE '%' || :query || '%')
        ORDER BY fullName ASC
    """)
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Long): ClientEntity?

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    fun getClientByIdFlow(id: Long): Flow<ClientEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("SELECT COUNT(*) FROM clients WHERE isActive = 1")
    fun getActiveClientsCount(): Flow<Int>

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClient(id: Long)
}

@Dao
interface MembershipDao {
    @Query("SELECT * FROM membership_types WHERE isActive = 1")
    fun getAllMembershipTypes(): Flow<List<MembershipTypeEntity>>
    
    @Query("SELECT * FROM membership_types ORDER BY isActive DESC, name ASC")
    fun getAllMembershipTypesAdmin(): Flow<List<MembershipTypeEntity>>

    @Query("SELECT * FROM membership_types WHERE id = :id LIMIT 1")
    suspend fun getMembershipTypeById(id: Long): MembershipTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembershipType(type: MembershipTypeEntity): Long

    @Update
    suspend fun updateMembershipType(type: MembershipTypeEntity)

    @Query("DELETE FROM membership_types WHERE id = :id")
    suspend fun deleteMembershipType(id: Long)

    @Query("SELECT * FROM membership_purchases WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getPurchasesForClient(clientId: Long): Flow<List<MembershipPurchaseEntity>>

    @Query("SELECT * FROM membership_purchases WHERE clientId = :clientId ORDER BY createdAt DESC")
    suspend fun getPurchasesForClientSync(clientId: Long): List<MembershipPurchaseEntity>

    @Query("SELECT * FROM membership_purchases ORDER BY createdAt DESC")
    fun getAllPurchases(): Flow<List<MembershipPurchaseEntity>>

    @Query("SELECT * FROM membership_purchases WHERE id = :id LIMIT 1")
    suspend fun getPurchaseById(id: Long): MembershipPurchaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: MembershipPurchaseEntity): Long

    @Update
    suspend fun updatePurchase(purchase: MembershipPurchaseEntity)

    @Query("SELECT COUNT(*) FROM membership_purchases")
    fun getActiveMembershipsCount(): Flow<Int>
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY category ASC, name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY isActive DESC, category ASC, name ASC")
    fun getAllProductsAdmin(): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE isActive = 1 
        AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>
    
    @Query("""
        SELECT * FROM products 
        WHERE (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        ORDER BY isActive DESC, name ASC
    """)
    fun searchProductsAdmin(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM product_sales WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getSalesForClient(clientId: Long): Flow<List<ProductSaleEntity>>

    @Query("SELECT * FROM product_sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<ProductSaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: ProductSaleEntity): Long
}

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits WHERE clientId = :clientId ORDER BY visitedAt DESC")
    fun getVisitsForClient(clientId: Long): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits ORDER BY visitedAt DESC")
    fun getAllVisits(): Flow<List<VisitEntity>>
    
    @Query("SELECT * FROM visits WHERE id = :id LIMIT 1")
    suspend fun getVisitById(id: Long): VisitEntity?

    @Query("DELETE FROM visits WHERE id = :id")
    suspend fun deleteVisit(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity): Long

    @Query("SELECT COUNT(*) FROM visits WHERE visitedAt >= :startOfDay")
    fun getTodayVisitsCount(startOfDay: String): Flow<Int>
}
