package com.example.beanflow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beanflow.data.User
import com.example.beanflow.data.UserDao
import kotlinx.coroutines.launch

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    // State untuk Input Login
    var usernameInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")

    // Status Login (Berhasil/Gagal)
    var loginStatus by mutableStateOf("")

    // Status apakah user sudah login atau belum
    var isLoggedIn by mutableStateOf(false)

    // Menyimpan Role User yang sedang login (Admin / Cashier)
    var userRole by mutableStateOf("")

    // Fungsi: Buat Admin Default jika belum ada (Dijalankan saat aplikasi mulai)
    fun initDefaultAdmin() {
        viewModelScope.launch {
            val admin = userDao.getUserByUsername("admin")
            if (admin == null) {
                // Perhatikan: sekarang pakai 'password', bukan 'passwordHash'
                // Dan pakai 'insertUser', bukan 'insert'
                userDao.insertUser(User(username = "admin", password = "admin123", role = "Admin"))
                userDao.insertUser(User(username = "kasir1", password = "123", role = "Cashier"))
            }
        }
    }

    // Fungsi: Proses Login
    fun login() {
        viewModelScope.launch {
            val user = userDao.getUserByUsername(usernameInput)

            if (user != null && user.password == passwordInput) {
                // Login Berhasil
                loginStatus = "Login Berhasil!"
                userRole = user.role
                isLoggedIn = true
            } else {
                // Login Gagal
                loginStatus = "Username atau Password salah!"
                isLoggedIn = false
            }
        }
    }
}