package com.example.beanflow.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int, // ID Kasir yang melayani

    @ColumnInfo(name = "transaction_date")
    val transactionDate: Long, // Kita simpan waktu dalam format Long (Milliseconds)

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String // "Tunai" atau "QRIS"
)