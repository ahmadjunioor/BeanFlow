package com.example.beanflow.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0, // Ganti val jadi var

    // ID Khusus Firebase
    @Ignore
    var firestoreId: String = "",

    @ColumnInfo(name = "name")
    var name: String = "", // Ganti val jadi var

    @ColumnInfo(name = "price")
    var price: Double = 0.0, // Ganti val jadi var

    @ColumnInfo(name = "stock")
    var stock: Int = 0, // Ganti val jadi var

    @ColumnInfo(name = "category")
    var category: String = "", // Ganti val jadi var

    @ColumnInfo(name = "image_path")
    var imagePath: String? = null, // Ganti val jadi var

    @ColumnInfo(name = "is_active")
    var isActive: Boolean = true // Ganti val jadi var
) {
    // Constructor kosong wajib untuk Firebase
    constructor() : this(0, "", "", 0.0, 0, "", null, true)
}