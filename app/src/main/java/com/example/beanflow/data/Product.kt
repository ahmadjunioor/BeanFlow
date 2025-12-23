package com.example.beanflow.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "stock")
    val stock: Int,

    @ColumnInfo(name = "category")
    val category: String, // Contoh: "Coffee", "Non-Coffee"

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null, // Boleh kosong (null)

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true // Untuk fitur Soft Delete
)