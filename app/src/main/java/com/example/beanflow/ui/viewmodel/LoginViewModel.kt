package com.example.beanflow.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.beanflow.data.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginViewModel : ViewModel() {
    // Hapus DAO, ganti ke Firebase
    private val db = Firebase.firestore

    var usernameInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")

    var isLoggedIn by mutableStateOf(false)
    var userRole by mutableStateOf("") // "Admin" atau "Cashier"
    var loginStatus by mutableStateOf("")

    // Fungsi Login ke Firebase
    fun login() {
        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            loginStatus = "Username/Password tidak boleh kosong"
            return
        }

        loginStatus = "Memeriksa..."

        // Cari di koleksi "users"
        db.collection("users")
            .whereEqualTo("username", usernameInput)
            .whereEqualTo("password", passwordInput)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Login Sukses
                    val user = documents.documents[0].toObject(User::class.java)
                    userRole = user?.role ?: "Cashier"
                    isLoggedIn = true
                    loginStatus = "Login Berhasil!"
                } else {
                    loginStatus = "Username atau Password salah!"
                }
            }
            .addOnFailureListener {
                loginStatus = "Error: ${it.message}"
            }
    }

    // Fungsi Membuat Admin Otomatis (Jika database kosong)
    fun initDefaultAdmin() {
        db.collection("users")
            .whereEqualTo("role", "Admin")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Belum ada admin, buat satu default
                    val admin = hashMapOf(
                        "username" to "admin",
                        "password" to "admin123",
                        "role" to "Admin"
                    )
                    db.collection("users").add(admin)
                }
            }
    }
}