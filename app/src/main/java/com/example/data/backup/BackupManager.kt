package com.example.data.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.local.ClientEntity
import com.example.data.local.CurrencyEntity
import com.example.data.local.GymDatabase
import com.example.data.local.MembershipPurchaseEntity
import com.example.data.local.MembershipTypeEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ProductSaleEntity
import com.example.data.local.VisitEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val context: Context, private val db: GymDatabase) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(GymBackup::class.java)

    // ───────────────── EXPORT ─────────────────

    suspend fun exportBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
            val fileName = "gymtrack_backup_$timestamp.json"

            val clients = db.clientDao().getAllClientsSync()
            val membershipTypes = db.membershipDao().getAllMembershipTypesSync()
            val purchases = db.membershipDao().getAllPurchasesSync()
            val products = db.productDao().getAllProductsSync()
            val sales = db.saleDao().getAllSalesSync()
            val visits = db.visitDao().getAllVisitsSync()
            val currencies = db.currencyDao().getAllCurrenciesSync()

            val backup = GymBackup(
                exportedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
                clients = clients.map {
                    ClientBackup(it.id, it.clientCode, it.fullName, it.phone, it.photoUrl, it.note, it.isActive, it.createdAt)
                },
                membershipTypes = membershipTypes.map {
                    MembershipTypeBackup(it.id, it.name, it.durationType, it.durationValue, it.price, it.isActive)
                },
                membershipPurchases = purchases.map {
                    MembershipPurchaseBackup(it.id, it.clientId, it.membershipTypeId, it.amountPaid, it.startsAt, it.expiresAt, it.visitsLeft, it.totalVisits, it.paymentMethod, it.createdAt)
                },
                products = products.map {
                    ProductBackup(it.id, it.name, it.category, it.price, it.stockQuantity, it.isActive)
                },
                productSales = sales.map {
                    ProductSaleBackup(it.id, it.clientId, it.productId, it.quantity, it.totalPrice, it.paymentMethod, it.createdAt)
                },
                visits = visits.map {
                    VisitBackup(it.id, it.clientId, it.membershipPurchaseId, it.visitedAt)
                },
                currencies = currencies.map {
                    CurrencyBackup(it.id, it.name, it.code, it.isSelected)
                }
            )

            val json = adapter.toJson(backup)

            val savedUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToDownloadsQ(fileName, json)
            } else {
                saveToDownloadsLegacy(fileName, json)
            }

            Result.success(fileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveToDownloadsQ(fileName: String, json: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { out -> out.write(json.toByteArray()) }
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(it, contentValues, null, null)
        }
        return uri
    }

    @Suppress("DEPRECATION")
    private fun saveToDownloadsLegacy(fileName: String, json: String): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadsDir.mkdirs()
        val file = java.io.File(downloadsDir, fileName)
        file.writeText(json)
        return file.absolutePath
    }

    // ───────────────── IMPORT ─────────────────

    suspend fun importBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Не удалось открыть файл"))

            val json = inputStream.bufferedReader().use { it.readText() }
            val backup = adapter.fromJson(json)
                ?: return@withContext Result.failure(Exception("Неверный формат файла"))

            // Clear existing operational data (keep tariffs/products/currencies)
            db.clientDao().deleteAll()
            db.visitDao().deleteAll()
            db.membershipDao().deleteAllPurchases()
            db.saleDao().deleteAll()

            // Restore clients
            backup.clients.forEach {
                db.clientDao().insertClientWithId(ClientEntity(it.id, it.clientCode, it.fullName, it.phone, it.photoUrl, it.note, it.isActive, it.createdAt))
            }

            // Restore membership types (merge — don't duplicate)
            backup.membershipTypes.forEach {
                db.membershipDao().insertMembershipTypeWithId(MembershipTypeEntity(it.id, it.name, it.durationType, it.durationValue, it.price, it.isActive))
            }

            // Restore purchases
            backup.membershipPurchases.forEach {
                db.membershipDao().insertPurchaseWithId(MembershipPurchaseEntity(it.id, it.clientId, it.membershipTypeId, it.amountPaid, it.startsAt, it.expiresAt, it.visitsLeft, it.totalVisits, it.paymentMethod, it.createdAt))
            }

            // Restore products
            backup.products.forEach {
                db.productDao().insertProductWithId(ProductEntity(it.id, it.name, it.category, it.price, it.stockQuantity, it.isActive))
            }

            // Restore sales
            backup.productSales.forEach {
                db.saleDao().insertSaleWithId(ProductSaleEntity(it.id, it.clientId, it.productId, it.quantity, it.totalPrice, it.paymentMethod, it.createdAt))
            }

            // Restore visits
            backup.visits.forEach {
                db.visitDao().insertVisitWithId(VisitEntity(it.id, it.clientId, it.membershipPurchaseId, it.visitedAt))
            }

            // Restore currencies
            backup.currencies.forEach {
                db.currencyDao().insertCurrencyWithId(CurrencyEntity(it.id, it.name, it.code, it.isSelected))
            }

            val clientCount = backup.clients.size
            Result.success("Восстановлено $clientCount клиентов и все связанные данные")
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка импорта: ${e.message}"))
        }
    }
}
