package com.example.beanflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.beanflow.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    // REVISI 1: Hanya tampilkan produk yang AKTIF (is_active = 1)
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    // REVISI 2: Jangan hapus data, tapi ubah statusnya jadi 0 (Soft Delete)
    @Query("UPDATE products SET is_active = 0 WHERE id = :productId")
    suspend fun softDeleteProduct(productId: Int)

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
    suspend fun decreaseStock(productId: Int, quantity: Int)
}