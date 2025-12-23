package com.example.beanflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beanflow.data.AppDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1. DATABASE SETUP (WAJIB PALING ATAS) ---
        val database = AppDatabase.getDatabase(this)

        // --- 2. VIEWMODEL SETUP ---
        val historyViewModel = HistoryViewModel(database.transactionDao())
        val loginViewModel = LoginViewModel(database.userDao())
        val adminViewModel = AdminViewModel(database.productDao(), database.transactionDao())
        val cashierViewModel = CashierViewModel(database.productDao(), database.transactionDao())
        val settingsViewModel = SettingsViewModel(database.userDao())

        // Setup UserViewModel untuk fitur kelola kasir
        val userViewModel = UserViewModel(database.userDao())

        // Seed Admin Default (jika belum ada)
        loginViewModel.initDefaultAdmin()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    if (loginViewModel.isLoggedIn) {
                        var activeScreen by remember { mutableStateOf("Dashboard") }

                        // --- NAVIGASI ---
                        when (activeScreen) {
                            "History" -> {
                                HistoryScreen(historyViewModel) { activeScreen = "Dashboard" }
                            }
                            "UserManagement" -> {
                                UserManagementScreen(userViewModel) { activeScreen = "Dashboard" }
                            }
                            "Settings" -> { // LAYAR BARU
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    userId = 1, // Di aplikasi real, ambil ID dari LoginViewModel
                                    username = loginViewModel.usernameInput, // Ambil username yg sedang login
                                    onBack = { activeScreen = "Dashboard" }
                                )
                            }
                            else -> {
                                // DASHBOARD
                                if (loginViewModel.userRole == "Admin") {
                                    Column {
                                        // --- TOMBOL PENGATURAN (BARU) ---
                                        Button(
                                            onClick = { activeScreen = "Settings" },
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(top = 8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                        ) {
                                            Text("PENGATURAN AKUN ")
                                        }
                                        // --------------------------------

                                        Row(Modifier.padding(horizontal = 8.dp)) {
                                            // Tombol History
                                            Button(
                                                onClick = { activeScreen = "History" },
                                                modifier = Modifier.weight(1f).padding(end = 4.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                            ) { Text("RIWAYAT") }

                                            // Tombol Kelola Kasir
                                            Button(
                                                onClick = { activeScreen = "UserManagement" },
                                                modifier = Modifier.weight(1f).padding(start = 4.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
                                            ) { Text("KASIR") }
                                        }

                                        AdminScreen(
                                            viewModel = adminViewModel,
                                            onLogout = { logout(loginViewModel) }
                                        )
                                    }
                                } else {
                                    // Layar Kasir (Tetap sama)
                                    CashierScreen(
                                        viewModel = cashierViewModel,
                                        userId = 1,
                                        onLogout = { logout(loginViewModel) }
                                    )
                                }
                            }
                        }
                    } else {
                        LoginScreen(loginViewModel)
                    }
                }
            }
        }
    }

    // Fungsi Helper Logout (Membersihkan state)
    private fun logout(viewModel: LoginViewModel) {
        viewModel.isLoggedIn = false
        viewModel.loginStatus = ""
        viewModel.usernameInput = ""
        viewModel.passwordInput = ""
    }
}

// --- UI LOGIN ---
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("BeanFlow Cashier", fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.usernameInput,
            onValueChange = { viewModel.usernameInput = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.passwordInput,
            onValueChange = { viewModel.passwordInput = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.loginStatus.isNotEmpty()) {
            Text(viewModel.loginStatus, color = MaterialTheme.colorScheme.primary)
        }
    }
}