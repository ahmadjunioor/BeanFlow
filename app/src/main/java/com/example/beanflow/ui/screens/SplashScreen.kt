package com.example.beanflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.beanflow.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // 1. Setup Animasi
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_coffee))

    // 2. Timer (Waktu Tunggu)
    LaunchedEffect(Unit) {
        delay(3000) // Tunggu 3 detik (3000 ms)
        onTimeout() // Panggil fungsi untuk pindah layar
    }

    // 3. Tampilan Layar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Latar belakang putih
        contentAlignment = Alignment.Center
    ) {
        // Animasi Lottie
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(250.dp)
        )

        // Teks opsional di bawah animasi
        Text(
            text = "BeanFlow",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)
        )
    }
}