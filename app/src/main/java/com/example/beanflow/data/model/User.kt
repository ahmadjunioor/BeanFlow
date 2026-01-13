package com.example.beanflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties // Mencegah error jika ada field asing di database
@Entity(tableName = "users")
data class User(
    // --- KHUSUS ROOM (Lokal) ---
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    // --- DATA USER ---
    var username: String = "",
    var password: String = "",
    var role: String = "Cashier", // Set default role

    // --- KHUSUS FIRESTORE (Online) ---
    // Variabel ini hanya untuk menampung ID Dokumen saat kita ambil data (Fetch).
    // @get:Exclude -> Jangan tulis variabel ini ke dalam "isi" data di server saat save/update.
    @get:Exclude
    var firestoreId: String = ""
)