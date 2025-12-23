package com.example.beanflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String, // <--- KOLOM INI WAJIB ADA
    val role: String      // Contoh: "Admin" atau "Cashier"
)