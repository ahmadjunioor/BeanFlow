package com.example.beanflow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beanflow.data.UserDao
import kotlinx.coroutines.launch

class SettingsViewModel(private val userDao: UserDao) : ViewModel() {

    var currentPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var message by mutableStateOf("")
    var isSuccess by mutableStateOf(false)

    // Fungsi Ganti Password
    // Kita butuh 'username' user yang sedang login untuk verifikasi pass lama
    fun changePassword(userId: Int, username: String) {
        viewModelScope.launch {
            // 1. Cek User di DB
            val user = userDao.getUserByUsername(username)

            if (user != null) {
                // 2. Validasi Password Lama
                if (user.password != currentPassword) {
                    message = "Password lama salah!"
                    isSuccess = false
                    return@launch
                }

                // 3. Validasi Password Baru
                if (newPassword.isEmpty() || newPassword.length < 3) {
                    message = "Password baru terlalu pendek!"
                    isSuccess = false
                    return@launch
                }

                // 4. Cek Konfirmasi Password
                if (newPassword != confirmPassword) {
                    message = "Konfirmasi password tidak cocok!"
                    isSuccess = false
                    return@launch
                }

                // 5. Simpan ke Database
                userDao.updatePassword(userId, newPassword)
                message = "Password berhasil diubah!"
                isSuccess = true

                // Reset Form
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
            }
        }
    }

    fun resetMessage() {
        message = ""
        isSuccess = false
    }
}