package com.example.beanflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.beanflow.data.model.Product
import com.example.beanflow.data.model.Transaction
import com.example.beanflow.data.model.TransactionDetail
import com.example.beanflow.data.model.User

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