package com.example.beanflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.beanflow.data.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _userList = MutableStateFlow<List<User>>(emptyList())
    val userList: StateFlow<List<User>> = _userList

    init {
        fetchUsers()
    }

    // 1. FETCH: Wajib mengambil ID Dokumen agar bisa di-Update/Delete
    private fun fetchUsers() {
        db.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { doc ->
                        val u = doc.toObject(User::class.java)
                        // PENTING: Masukkan ID dokumen Firestore ke dalam objek User
                        // Pastikan di User.kt ada: var firestoreId: String = ""
                        u?.apply {
                            firestoreId = doc.id
                        }
                    }
                    _userList.value = users
                }
            }
    }

    // 2. ADD: Tambah User Baru
    fun addUser(user: User) {
        val newUserMap = hashMapOf(
            "username" to user.username,
            "password" to user.password,
            "role" to user.role
        )
        db.collection("users").add(newUserMap)
    }

    // 3. UPDATE: Fungsi Update (Fitur Baru)
    fun updateUser(user: User) {
        // Kita butuh map data yang baru
        val updateMap = mapOf(
            "username" to user.username,
            "password" to user.password,
            "role" to user.role
        )

        // Pastikan firestoreId tidak kosong (didapat dari fetchUsers)
        if (user.firestoreId.isNotEmpty()) {
            db.collection("users").document(user.firestoreId)
                .update(updateMap)
                .addOnSuccessListener {
                    // Update berhasil (UI akan otomatis berubah karena ada SnapshotListener)
                }
                .addOnFailureListener { e ->
                    // Handle error jika perlu
                    println("Error updating user: $e")
                }
        } else {
            println("Error: Tidak bisa update karena ID kosong.")
        }
    }

    // 4. DELETE: Hapus User (Sekarang lebih aman pakai ID)
    fun deleteUser(user: User) {
        if (user.firestoreId.isNotEmpty()) {
            // Hapus berdasarkan ID (Paling Akurat)
            db.collection("users").document(user.firestoreId).delete()
        } else {
            // Fallback: Hapus berdasarkan username (Cara Lama)
            db.collection("users")
                .whereEqualTo("username", user.username)
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs) {
                        db.collection("users").document(doc.id).delete()
                    }
                }
        }
    }
}