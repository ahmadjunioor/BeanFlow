package com.example.beanflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // 1. Fungsi Login (Cari user berdasarkan username)
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    // 2. Fungsi Tambah User (INI YANG HILANG SEBELUMNYA)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // 3. Ambil daftar semua kasir (Role = 'Cashier')
    @Query("SELECT * FROM users WHERE role = 'Cashier'")
    fun getAllCashiers(): Flow<List<User>>

    // 4. Hapus User (Kasir)
    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Int, newPassword: String)
}