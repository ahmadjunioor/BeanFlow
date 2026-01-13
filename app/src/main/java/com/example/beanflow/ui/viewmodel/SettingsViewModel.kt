package com.example.beanflow.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SettingsViewModel : ViewModel() {
    private val db = Firebase.firestore

    var currentPasswordInput by mutableStateOf("")
    var newPasswordInput by mutableStateOf("")
    var confirmPasswordInput by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf("")

    // Fungsi Ganti Password
    fun changePassword(username: String) {
        if (currentPasswordInput.isEmpty() || newPasswordInput.isEmpty()) {
            statusMessage = "Password tidak boleh kosong"
            return
        }

        if (newPasswordInput != confirmPasswordInput) {
            statusMessage = "Password baru tidak cocok!"
            return
        }

        isLoading = true
        statusMessage = "Memproses..."

        // 1. Cari User berdasarkan Username
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    statusMessage = "User tidak ditemukan!"
                    isLoading = false
                    return@addOnSuccessListener
                }

                val userDoc = documents.documents[0]
                val currentDbPass = userDoc.getString("password") ?: ""

                // 2. Cek apakah password lama benar
                if (currentDbPass != currentPasswordInput) {
                    statusMessage = "Password lama salah!"
                    isLoading = false
                } else {
                    // 3. Update Password Baru
                    db.collection("users").document(userDoc.id)
                        .update("password", newPasswordInput)
                        .addOnSuccessListener {
                            statusMessage = "Berhasil ganti password!"
                            isLoading = false
                            // Reset field
                            currentPasswordInput = ""
                            newPasswordInput = ""
                            confirmPasswordInput = ""
                        }
                        .addOnFailureListener {
                            statusMessage = "Gagal: ${it.message}"
                            isLoading = false
                        }
                }
            }
            .addOnFailureListener {
                statusMessage = "Error koneksi: ${it.message}"
                isLoading = false
            }
    }
}