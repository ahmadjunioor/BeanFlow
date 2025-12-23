package com.example.beanflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// PERHATIKAN: version berubah jadi 2, dan entities bertambah
@Database(
    entities = [User::class, Product::class, Transaction::class, TransactionDetail::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    // Nanti kita tambah TransactionDao di sini
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beanflow_database"
                )
                    .fallbackToDestructiveMigration() // PENTING: Ini akan menghapus data lama karena versi naik
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}