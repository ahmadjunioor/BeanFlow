package com.example.beanflow.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.beanflow.ui.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- WARNA TEMA KHUSUS ADMIN ---
private val AdminCoffeeDark = Color(0xFF4E342E)
private val AdminCoffeeMedium = Color(0xFF795548)
private val AdminCream = Color(0xFFFFF3E0)
private val AdminCoffeeLight = Color(0xFFD7CCC8)
private val AdminPageBackground = Brush.verticalGradient(listOf(AdminCream, Color.White))

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    var showStockDialog by remember { mutableStateOf(false) }
    var showTransDialog by remember { mutableStateOf(false) }

    // Mengambil Data Real dari ViewModel
    val dailyRevenue = viewModel.dailyRevenue
    val monthlyRevenue = viewModel.monthlyRevenue

    Box(modifier = Modifier.fillMaxSize().background(AdminPageBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // --- HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dashboard Admin", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = AdminCoffeeDark)
                    Text("Pantau performa toko", fontSize = 14.sp, color = AdminCoffeeMedium)
                }
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- STATISTIK ---
            RevenueCardSplit(
                dailyTotal = formatRp.format(dailyRevenue),
                monthlyTotal = formatRp.format(monthlyRevenue),
                color1 = AdminCoffeeDark,
                color2 = AdminCoffeeMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // UPDATE DI SINI: Menggunakan 'dailyTransactionCount' & Judul "Transaksi Hari Ini"
                StatCardSmall(
                    title = "Transaksi Hari Ini",
                    value = "${viewModel.dailyTransactionCount}",
                    icon = Icons.Default.List,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f),
                    onClick = { showTransDialog = true }
                )
                StatCardSmall(
                    title = "Stok Menipis",
                    value = "${viewModel.lowStockCount}",
                    icon = Icons.Default.Warning,
                    color = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f),
                    onClick = { showStockDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // --- MENU UTAMA ---
            Text("Aksi Cepat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminCoffeeDark)
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MenuActionCard("Buka Kasir", "Mode Admin", Icons.Default.ShoppingCart, AdminCoffeeDark, Modifier.weight(1f)) { onNavigate("CashierModeAdmin") }
                    MenuActionCard("Kelola Staf", "Data User", Icons.Default.Person, AdminCoffeeMedium, Modifier.weight(1f)) { onNavigate("UserManagement") }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MenuActionCard("Laporan", "Riwayat", Icons.Default.DateRange, AdminCoffeeMedium, Modifier.weight(1f)) { onNavigate("History") }
                    MenuActionCard("Pengaturan", "Akun Admin", Icons.Default.Settings, Color.Gray, Modifier.weight(1f)) { onNavigate("Settings") }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- DIALOGS ---
    if (showStockDialog) ResponsiveListDialog("Stok Menipis", { showStockDialog = false }) {
        if (viewModel.lowStockList.isEmpty()) Text("Stok aman terkendali!", modifier = Modifier.padding(16.dp), color = AdminCoffeeDark)
        else viewModel.lowStockList.forEach { ListItem(headlineContent = { Text(it["name"] as String, fontWeight = FontWeight.Bold) }, trailingContent = { Text("${it["stock"]} pcs", color = Color.Red, fontWeight = FontWeight.Bold) }); Divider(color = AdminCoffeeLight.copy(0.3f)) }
    }

    if (showTransDialog) ResponsiveListDialog("Transaksi Terakhir", { showTransDialog = false }) {
        if (viewModel.recentTransactions.isEmpty()) {
            Text("Belum ada transaksi hari ini.", modifier = Modifier.padding(16.dp), color = AdminCoffeeDark)
        } else {
            viewModel.recentTransactions.forEach {
                val sdf = SimpleDateFormat("dd MMM HH:mm", Locale("id", "ID"))
                ListItem(
                    headlineContent = {
                        Text(formatRp.format(it["amount"]), fontWeight = FontWeight.Bold, color = AdminCoffeeDark)
                    },
                    supportingContent = {
                        Text(it["method"] as String + " • " + sdf.format(Date(it["date"] as Long)))
                    },
                    leadingContent = {
                        Icon(Icons.Default.CheckCircle, null, tint = AdminCoffeeMedium)
                    }
                )
                Divider(color = AdminCoffeeLight.copy(0.3f))
            }
        }
    }
}

// ================= KOMPONEN UI =================

@Composable
fun RevenueCardSplit(dailyTotal: String, monthlyTotal: String, color1: Color, color2: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(color1, color2))).padding(24.dp).fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(0.2f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Total Pendapatan", color = Color.White.copy(0.9f), fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SPLIT ROW: Harian & Bulanan
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Hari Ini", fontSize = 12.sp, color = Color.White.copy(0.7f))
                        Text(dailyTotal, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(0.3f)))

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Bulan Ini", fontSize = 12.sp, color = Color.White.copy(0.7f))
                        Text(monthlyTotal, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCardSmall(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(130.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.1f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Column {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AdminCoffeeDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(title, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MenuActionCard(title: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(100.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminCoffeeDark, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
                Text(text = subtitle, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun ResponsiveListDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White, titleContentColor = AdminCoffeeDark,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = { Column(modifier = Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState())) { content() } },
        confirmButton = { Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AdminCoffeeDark)) { Text("Tutup") } },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}