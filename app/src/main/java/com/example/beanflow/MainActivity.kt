package com.example.beanflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.beanflow.data.local.AppDatabase
import com.example.beanflow.ui.screens.AdminScreen
import com.example.beanflow.ui.screens.CashierScreen
import com.example.beanflow.ui.screens.HistoryScreen
import com.example.beanflow.ui.screens.LoginScreen
import com.example.beanflow.ui.screens.SettingsScreen
import com.example.beanflow.ui.screens.SplashScreen
import com.example.beanflow.ui.screens.UserManagementScreen
import com.example.beanflow.ui.viewmodel.AdminViewModel
import com.example.beanflow.ui.viewmodel.CashierViewModel
import com.example.beanflow.ui.viewmodel.HistoryViewModel
import com.example.beanflow.ui.viewmodel.LoginViewModel
import com.example.beanflow.ui.viewmodel.SettingsViewModel
import com.example.beanflow.ui.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup Database & ViewModel
        val database = AppDatabase.getDatabase(this)
        val historyViewModel = HistoryViewModel()
        val loginViewModel = LoginViewModel()
        val adminViewModel = AdminViewModel()
        val cashierViewModel = CashierViewModel(database.productDao(), database.transactionDao())
        val settingsViewModel = SettingsViewModel()
        val userViewModel = UserViewModel()

        loginViewModel.initDefaultAdmin()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    // STATE: Apakah Splash Screen sedang tampil?
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        // 1. TAMPILKAN SPLASH SCREEN DULU
                        SplashScreen(onTimeout = {
                            showSplash = false // Setelah 3 detik, matikan splash screen
                        })
                    } else {
                        // 2. MASUK KE LOGIKA UTAMA (Login / Dashboard)
                        if (loginViewModel.isLoggedIn) {
                            var activeScreen by remember { mutableStateOf("Dashboard") }

                            when (activeScreen) {
                                "History" -> HistoryScreen(historyViewModel) { activeScreen = "Dashboard" }
                                "UserManagement" -> UserManagementScreen(userViewModel) { activeScreen = "Dashboard" }
                                "Settings" -> SettingsScreen(
                                    viewModel = settingsViewModel,
                                    userId = 1,
                                    username = loginViewModel.usernameInput,
                                    onBack = { activeScreen = "Dashboard" }
                                )
                                // UPDATE: Kasus Baru untuk Admin masuk ke Mode Kasir
                                "CashierModeAdmin" -> {
                                    CashierScreen(
                                        viewModel = cashierViewModel,
                                        userId = 1,
                                        isAdmin = true, // <--- ADMIN BOLEH EDIT/HAPUS
                                        onLogout = { activeScreen = "Dashboard" } // Kalau keluar, balik ke Dashboard Admin
                                    )
                                }
                                else -> {
                                    // DASHBOARD UTAMA
                                    if (loginViewModel.userRole == "Admin") {
                                        // PERBAIKAN DI SINI:
                                        // 1. Hapus semua tombol manual (Button) yang menumpuk.
                                        // 2. Panggil AdminScreen dengan parameter 'onNavigate'.

                                        AdminScreen(
                                            viewModel = adminViewModel,
                                            onNavigate = { destination ->
                                                activeScreen = destination // <--- Ini menyambungkan tombol menu ke navigasi
                                            },
                                            onLogout = { logout(loginViewModel) }
                                        )

                                    } else {
                                        // LAYAR KASIR BIASA (KARYAWAN)
                                        CashierScreen(
                                            viewModel = cashierViewModel,
                                            userId = 1,
                                            isAdmin = false, // <--- KARYAWAN TIDAK BOLEH EDIT
                                            onLogout = { logout(loginViewModel) }
                                        )
                                    }
                                }
                            }
                        } else {
                            // TAMPILKAN LOGIN SCREEN
                            LoginScreen(loginViewModel) {}
                        }
                    }
                }
            }
        }
    }

    private fun logout(viewModel: LoginViewModel) {
        viewModel.isLoggedIn = false
        viewModel.loginStatus = ""
        viewModel.usernameInput = ""
        viewModel.passwordInput = ""
    }
}