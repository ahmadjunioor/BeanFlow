package com.example.beanflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.beanflow.data.Product
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(viewModel: CashierViewModel, userId: Int, onLogout: () -> Unit) {
    val products by viewModel.filteredProducts.collectAsState()
    val cartItems = viewModel.cartItems
    val context = LocalContext.current

    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    // STATE UNTUK DIALOG
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // STATE DATA TRANSAKSI TERAKHIR
    var lastTotal by remember { mutableStateOf(0.0) }
    var lastCash by remember { mutableStateOf(0.0) }
    var lastChange by remember { mutableStateOf(0.0) }
    var lastMethod by remember { mutableStateOf("") } // Simpan metode bayar terakhir

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kasir BeanFlow") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color.Red)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // --- BAGIAN KIRI: MENU (60%) ---
            Box(modifier = Modifier.weight(0.6f).padding(8.dp)) {
                Column {
                    OutlinedTextField(
                        value = viewModel.searchQuery,
                        onValueChange = { viewModel.searchQuery = it },
                        label = { Text("Cari Menu...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        items(viewModel.categories) { category ->
                            CategoryChip(
                                label = category,
                                isSelected = category == viewModel.selectedCategory,
                                onClick = { viewModel.selectedCategory = category }
                            )
                        }
                    }

                    if (products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            val msg = if (viewModel.selectedCategory == "All") "Belum ada produk." else "Tidak ada produk di kategori '${viewModel.selectedCategory}'"
                            Text(text = msg, color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                            items(products) { product ->
                                MenuCard(product, formatRp) {
                                    val isSuccess = viewModel.addToCart(product)
                                    if (!isSuccess) {
                                        Toast.makeText(context, "Stok tidak mencukupi!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- BAGIAN KANAN: KERANJANG (40%) ---
            Column(modifier = Modifier.weight(0.4f).background(Color(0xFFF5F5F5)).padding(16.dp)) {
                Text("Keranjang Belanja", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cartItems) { item ->
                        CartItemRow(item, formatRp,
                            onRemove = { viewModel.removeItem(item) },
                            onDecrease = { viewModel.decreaseItem(item) }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total :", fontWeight = FontWeight.Bold)
                    Text(formatRp.format(viewModel.totalAmount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showCheckoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = cartItems.isNotEmpty()
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("BAYAR SEKARANG")
                }
            }
        }

        // --- 1. DIALOG INPUT BAYAR (UPDATE) ---
        if (showCheckoutDialog) {
            CheckoutDialog(
                totalAmount = viewModel.totalAmount,
                onDismiss = { showCheckoutDialog = false },
                onConfirm = { cash, change, method -> // Terima parameter method
                    lastTotal = viewModel.totalAmount
                    lastCash = cash
                    lastChange = change
                    lastMethod = method

                    // Kirim metode pembayaran ke ViewModel
                    viewModel.processTransaction(userId, method) {
                        showCheckoutDialog = false
                        showSuccessDialog = true
                    }
                }
            )
        }

        // --- 2. DIALOG SUKSES (STRUK) ---
        if (showSuccessDialog) {
            SuccessDialog(
                total = lastTotal,
                cash = lastCash,
                change = lastChange,
                method = lastMethod, // Tampilkan metode
                onDismiss = { showSuccessDialog = false }
            )
        }
    }
}

// --- KOMPONEN PENDUKUNG ---

@Composable
fun CheckoutDialog(
    totalAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double, String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Tunai") }
    val paymentMethods = listOf("Tunai", "QRIS", "Debit")

    var cashInput by remember { mutableStateOf("") }
    var change by remember { mutableStateOf(0.0) }
    var errorText by remember { mutableStateOf("") }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 } }

    fun calculateChange(input: String) {
        cashInput = input
        val cash = input.toDoubleOrNull() ?: 0.0
        change = cash - totalAmount
        errorText = if (cash < totalAmount) "Uang kurang!" else ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Pembayaran", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Total Tagihan:", fontSize = 14.sp, color = Color.Gray)
                Text(currencyFormat.format(totalAmount), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))

                // PILIHAN METODE PEMBAYARAN
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    paymentMethods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = {
                                selectedMethod = method
                                // Jika Non-Tunai, otomatis set uang pas
                                if (method != "Tunai") {
                                    cashInput = totalAmount.toInt().toString()
                                    change = 0.0
                                    errorText = ""
                                } else {
                                    cashInput = ""
                                    change = 0.0
                                }
                            },
                            label = { Text(method) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // INPUT UANG (Hanya muncul jika TUNAI)
                if (selectedMethod == "Tunai") {
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) calculateChange(it) },
                        label = { Text("Uang Diterima") },
                        placeholder = { Text("Cth: 50000") },
                        singleLine = true,
                        isError = errorText.isNotEmpty(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorText.isNotEmpty()) Text(errorText, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                } else {
                    // Tampilan jika QRIS/Debit
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Silakan lakukan pembayaran via ${selectedMethod}", textAlign = TextAlign.Center)
                            Text("Menunggu konfirmasi...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tampilan Kembalian (Hanya Tunai)
                if (selectedMethod == "Tunai") {
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kembalian:", fontWeight = FontWeight.SemiBold)
                        val displayChange = if (change < 0) 0.0 else change
                        Text(currencyFormat.format(displayChange), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (displayChange >= 0 && cashInput.isNotEmpty() && errorText.isEmpty()) Color(0xFF4CAF50) else Color.Black)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cash = if (selectedMethod == "Tunai") cashInput.toDoubleOrNull() ?: 0.0 else totalAmount
                    onConfirm(cash, change, selectedMethod)
                },
                enabled = (selectedMethod != "Tunai") || (errorText.isEmpty() && cashInput.isNotEmpty()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedMethod == "Tunai") "BAYAR & CETAK" else "KONFIRMASI LUNAS")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Batal") } }
    )
}

@Composable
fun SuccessDialog(total: Double, cash: Double, change: Double, method: String, onDismiss: () -> Unit) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 } }

    AlertDialog(
        onDismissRequest = {},
        icon = {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFF4CAF50))
        },
        title = {
            Text("Transaksi Berhasil!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (method == "Tunai") {
                    Text("KEMBALIAN", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = currencyFormat.format(change),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    Text("METODE PEMBAYARAN", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = method,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Tagihan")
                    Text(currencyFormat.format(total), fontWeight = FontWeight.Bold)
                }
                if (method == "Tunai") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tunai")
                        Text(currencyFormat.format(cash))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("TRANSAKSI BARU")
            }
        }
    )
}

// --- BAGIAN LAIN TIDAK BERUBAH ---
@Composable
fun MenuCard(product: Product, formatRp: NumberFormat, onClick: () -> Unit) {
    val isOutOfStock = product.stock <= 0

    Card(
        modifier = Modifier.padding(4.dp).fillMaxWidth(),
        onClick = { if (!isOutOfStock) onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isOutOfStock) Color(0xFFEEEEEE) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = product.name, fontWeight = FontWeight.Bold, maxLines = 1, color = if (isOutOfStock) Color.Gray else Color.Black)
            Text(product.category, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = formatRp.format(product.price), color = if (isOutOfStock) Color.Gray else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(text = "Stok: ${product.stock}", fontSize = 10.sp, color = if (isOutOfStock) Color.Red else Color.DarkGray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp),
                enabled = !isOutOfStock,
                colors = ButtonDefaults.buttonColors(containerColor = if (isOutOfStock) Color.LightGray else MaterialTheme.colorScheme.primary)
            ) {
                if (isOutOfStock) Text("HABIS", fontSize = 12.sp) else Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, contentColor = if (isSelected) Color.White else Color.Black),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) { Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
}

@Composable
fun CartItemRow(item: CartItem, formatRp: NumberFormat, onRemove: () -> Unit, onDecrease: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold)
                Text("${item.quantity} x ${formatRp.format(item.product.price)}", fontSize = 12.sp, color = Color.Gray)
            }
            Text(formatRp.format(item.subtotal), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp), fontSize = 14.sp)
            IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) { Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(20.dp)) }
        }
    }
}