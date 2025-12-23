package com.example.beanflow.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_details",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["transaction_id"])] // <--- TAMBAHAN UNTUK HILANGKAN WARNING
)
data class TransactionDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "transaction_id")
    val transactionId: Int, // Link ke tabel transactions

    @ColumnInfo(name = "product_name")
    val productName: String, // Kita simpan nama saat transaksi (jaga-jaga kalau nama menu berubah)

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "price_at_transaction")
    val price: Double, // Harga saat beli (bukan harga sekarang)

    @ColumnInfo(name = "subtotal")
    val subtotal: Double
)