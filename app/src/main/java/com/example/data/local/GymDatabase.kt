package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ClientEntity::class,
        MembershipTypeEntity::class,
        MembershipPurchaseEntity::class,
        ProductEntity::class,
        ProductSaleEntity::class,
        VisitEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun membershipDao(): MembershipDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun visitDao(): VisitDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getInstance(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_track_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
