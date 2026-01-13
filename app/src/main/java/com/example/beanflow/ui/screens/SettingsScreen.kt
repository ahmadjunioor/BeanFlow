package com.example.beanflow.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock // Kita pakai Lock untuk semua password
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beanflow.ui.viewmodel.SettingsViewModel

// --- WARNA TEMA KHUSUS SETTINGS ---
private val SettingsCoffeeDark = Color(0xFF4E342E)
private val SettingsCoffeeMedium = Color(0xFF795548)
private val SettingsCream = Color(0xFFFFF3E0)
private val SettingsBackground = Brush.verticalGradient(listOf(SettingsCream, Color.White))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    userId: Int,
    username: String,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Akun", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SettingsCoffeeDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(SettingsBackground)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Box(modifier = Modifier.weight(0.4f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                        SettingsHeader(username, isLandscape = true)
                    }
                    Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
                            ChangePasswordForm(viewModel, username)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())
                ) {
                    SettingsHeader(username, isLandscape = false)
                    Spacer(modifier = Modifier.height(24.dp))
                    ChangePasswordForm(viewModel, username)
                }
            }
        }
    }
}

@Composable
fun SettingsHeader(username: String, isLandscape: Boolean) {
    Column {
        Text(
            text = "Halo, $username",
            fontSize = if (isLandscape) 28.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            color = SettingsCoffeeDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Amankan akun Anda dengan mengganti password secara berkala.",
            color = SettingsCoffeeMedium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ChangePasswordForm(viewModel: SettingsViewModel, username: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Ganti Password", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SettingsCoffeeDark)
            Spacer(modifier = Modifier.height(16.dp))

            // Form Ganti Password
            OutlinedTextField(
                value = viewModel.currentPasswordInput,
                onValueChange = { viewModel.currentPasswordInput = it },
                label = { Text("Password Lama") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = SettingsCoffeeMedium) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SettingsCoffeeDark, focusedLabelColor = SettingsCoffeeDark, cursorColor = SettingsCoffeeDark)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.newPasswordInput,
                onValueChange = { viewModel.newPasswordInput = it },
                label = { Text("Password Baru") },
                // PERBAIKAN: Ganti VpnKey -> Lock agar tidak error
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = SettingsCoffeeMedium) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SettingsCoffeeDark, focusedLabelColor = SettingsCoffeeDark, cursorColor = SettingsCoffeeDark)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.confirmPasswordInput,
                onValueChange = { viewModel.confirmPasswordInput = it },
                label = { Text("Konfirmasi Password Baru") },
                // PERBAIKAN: Ganti VpnKey -> Lock agar tidak error
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = SettingsCoffeeMedium) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SettingsCoffeeDark, focusedLabelColor = SettingsCoffeeDark, cursorColor = SettingsCoffeeDark)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.changePassword(username) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = SettingsCoffeeDark)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SIMPAN PASSWORD", fontWeight = FontWeight.Bold)
                }
            }

            if (viewModel.statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                val isSuccess = viewModel.statusMessage.contains("Berhasil", ignoreCase = true)
                Surface(
                    color = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = viewModel.statusMessage,
                        color = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}