package com.example.beanflow.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.beanflow.R
import com.example.beanflow.ui.viewmodel.LoginViewModel

// --- PALET WARNA TEMA KOPI ---
val CoffeeDark = Color(0xFF4E342E)
val CoffeeMedium = Color(0xFF795548)
val CoffeeLight = Color(0xFFD7CCC8)
val Cream = Color(0xFFFFF3E0)
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(Cream, CoffeeLight)
)

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier.fillMaxSize().background(brush = BackgroundGradient)
    ) {
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(topEnd = 40.dp, bottomEnd = 40.dp)).background(CoffeeDark),
                    contentAlignment = Alignment.Center
                ) { LogoSection(isLandscape = true, textColor = Color.White) }

                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        LoginFormSection(viewModel, onLoginSuccess)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().weight(0.4f).clip(RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp)).background(CoffeeDark), contentAlignment = Alignment.Center) {
                    LogoSection(isLandscape = false, textColor = Color.White)
                }
                Box(modifier = Modifier.fillMaxWidth().weight(0.6f).padding(24.dp), contentAlignment = Alignment.TopCenter) {
                    LoginFormSection(viewModel, onLoginSuccess)
                }
            }
        }
    }
}

@Composable
fun LogoSection(isLandscape: Boolean, textColor: Color) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.login_anim)) // Pastikan file ini ada atau ganti R.raw.anim_coffee
    val progress by animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val animSize = if (isLandscape) 150.dp else 200.dp
        LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(animSize))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "BeanFlow", fontSize = if (isLandscape) 32.sp else 40.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = textColor, letterSpacing = 2.sp)
    }
}

@Composable
fun LoginFormSection(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    Card(elevation = CardDefaults.cardElevation(defaultElevation = 10.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), modifier = Modifier.widthIn(max = 400.dp)) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Selamat Datang", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoffeeDark)
            Text(text = "Silakan login akun Anda", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = viewModel.usernameInput, onValueChange = { viewModel.usernameInput = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CoffeeMedium) },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeDark, focusedLabelColor = CoffeeDark, cursorColor = CoffeeDark)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PERBAIKAN: Hapus referensi ke Icons.Filled.Visibility karena librarynya tidak ada
            // Kita gunakan Teks "Show" / "Hide" sementara agar error hilang dan UI tetap jalan
            OutlinedTextField(
                value = viewModel.passwordInput, onValueChange = { viewModel.passwordInput = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CoffeeMedium) },
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "HIDE" else "SHOW", color = CoffeeMedium, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoffeeDark, focusedLabelColor = CoffeeDark, cursorColor = CoffeeDark)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.login() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoffeeDark),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                enabled = viewModel.loginStatus != "Memeriksa..."
            ) {
                if (viewModel.loginStatus == "Memeriksa...") {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("MASUK", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            if (viewModel.loginStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(color = if (viewModel.isLoggedIn) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(text = viewModel.loginStatus, color = if (viewModel.isLoggedIn) Color(0xFF2E7D32) else Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(10.dp))
                }
            }

            LaunchedEffect(viewModel.isLoggedIn) {
                if (viewModel.isLoggedIn) {
                    onLoginSuccess()
                }
            }
        }
    }
}