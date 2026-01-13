@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.beanflow.ui.screens

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beanflow.ui.viewmodel.CashierViewModel
import com.example.beanflow.data.model.CartItem
import com.example.beanflow.data.model.Product
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- WARNA TEMA KHUSUS KASIR ---
private val ThemeCoffeeDark = Color(0xFF4E342E)
private val ThemeCoffeeMedium = Color(0xFF795548)
private val ThemeCream = Color(0xFFFFF3E0)
private val ThemeCoffeeLight = Color(0xFFD7CCC8)
private val ThemeBackground = Brush.verticalGradient(listOf(ThemeCream, Color.White))

@Composable
fun CashierScreen(
    viewModel: CashierViewModel,
    userId: Int,
    isAdmin: Boolean,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val products by viewModel.filteredProducts.collectAsState()
    val cartItems = viewModel.cartItems
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    // State untuk menyimpan data transaksi terakhir (agar bisa dicetak di struk)
    var lastTotal by remember { mutableStateOf(0.0) }
    var lastCash by remember { mutableStateOf(0.0) }
    var lastChange by remember { mutableStateOf(0.0) }
    var lastMethod by remember { mutableStateOf("") }
    var lastCartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) } // Simpan barang yang dibeli

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeCoffeeDark, titleContentColor = Color.White),
                title = {
                    Column {
                        Text("BeanFlow", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(if (isAdmin) "Mode Admin (Edit)" else "Mode Kasir", fontSize = 12.sp, color = ThemeCream)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) { Text("Kembali", color = ThemeCream, fontWeight = FontWeight.Bold) }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    containerColor = ThemeCoffeeDark,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Add, "Tambah") }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(ThemeBackground)) {
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(if (isAdmin) 1f else 0.65f).fillMaxHeight().padding(12.dp)) {
                        MenuSection(viewModel, products, formatRp, isAdmin, { p -> if (isAdmin) { productToEdit = p; showEditDialog = true } else { if (!viewModel.addToCart(p)) Toast.makeText(context, "Stok Habis!", Toast.LENGTH_SHORT).show() } }, { p -> productToEdit = p; showEditDialog = true }, { p -> viewModel.deleteProduct(p); Toast.makeText(context, "Dihapus", Toast.LENGTH_SHORT).show() })
                    }
                    if (!isAdmin) {
                        Box(modifier = Modifier.weight(0.35f).fillMaxHeight()) {
                            Row { Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = ThemeCoffeeLight); CartSection(cartItems, viewModel.totalAmount, formatRp, { viewModel.removeItem(it) }, { viewModel.decreaseItem(it) }, { showCheckoutDialog = true }) }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(if (isAdmin) 1f else 0.6f).padding(12.dp)) {
                        MenuSection(viewModel, products, formatRp, isAdmin, { p -> if (isAdmin) { productToEdit = p; showEditDialog = true } else { if (!viewModel.addToCart(p)) Toast.makeText(context, "Stok Habis!", Toast.LENGTH_SHORT).show() } }, { p -> productToEdit = p; showEditDialog = true }, { p -> viewModel.deleteProduct(p); Toast.makeText(context, "Dihapus", Toast.LENGTH_SHORT).show() })
                    }
                    if (!isAdmin) {
                        Box(modifier = Modifier.weight(0.4f)) {
                            CartSection(cartItems, viewModel.totalAmount, formatRp, { viewModel.removeItem(it) }, { viewModel.decreaseItem(it) }, { showCheckoutDialog = true })
                        }
                    }
                }
            }
        }

        // --- DIALOGS ---
        if (showCheckoutDialog) {
            CheckoutDialog(viewModel.totalAmount, { showCheckoutDialog = false }) { cash, change, method ->
                // Simpan data sebelum transaksi diproses (karena keranjang akan dikosongkan)
                lastTotal = viewModel.totalAmount
                lastCash = cash
                lastChange = change
                lastMethod = method
                lastCartItems = viewModel.cartItems.toList() // Copy list keranjang

                viewModel.processTransaction(userId, method) {
                    showCheckoutDialog = false
                    showSuccessDialog = true
                }
            }
        }

        if (showSuccessDialog) {
            SuccessDialog(
                lastCartItems, lastTotal, lastCash, lastChange, lastMethod,
                onDismiss = { showSuccessDialog = false },
                onPrint = {
                    // Panggil fungsi share receipt
                    shareReceipt(context, lastCartItems, lastTotal, lastCash, lastChange, lastMethod)
                }
            )
        }

        if (showAddProductDialog) AddProductDialog({ showAddProductDialog = false }) { name, cat, price, stock -> viewModel.addProductToFirebase(name, cat, price, stock); showAddProductDialog = false; Toast.makeText(context, "Disimpan", Toast.LENGTH_SHORT).show() }
        if (showEditDialog && productToEdit != null) EditProductDialog(productToEdit!!, { showEditDialog = false }) { name, cat, price, stock -> viewModel.updateProduct(productToEdit!!, name, cat, price, stock); showEditDialog = false; Toast.makeText(context, "Diupdate", Toast.LENGTH_SHORT).show() }
    }
}

// --- FUNGSI SHARE STRUK (PENTING) ---
fun shareReceipt(context: Context, items: List<CartItem>, total: Double, cash: Double, change: Double, method: String) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
    val date = dateFormat.format(Date())

    val sb = StringBuilder()
    sb.append("      BEANFLOW COFFEE      \n")
    sb.append("   Jl. Kopi Nikmat No. 1   \n")
    sb.append("---------------------------\n")
    sb.append("Tgl: $date\n")
    sb.append("---------------------------\n")

    items.forEach { item ->
        sb.append("${item.product.name}\n")
        sb.append("${item.quantity} x ${formatRp.format(item.product.price)} = ${formatRp.format(item.subtotal)}\n")
    }

    sb.append("---------------------------\n")
    sb.append("Total    : ${formatRp.format(total)}\n")
    sb.append("Bayar ($method): ${formatRp.format(cash)}\n")
    if (method == "Tunai") {
        sb.append("Kembali  : ${formatRp.format(change)}\n")
    }
    sb.append("---------------------------\n")
    sb.append("    Terima Kasih! :)\n")
    sb.append("   Simpan struk ini\n")
    sb.append("   sebagai bukti.\n")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Cetak/Kirim Struk"))
}

// --- KOMPONEN UI ---

@Composable
fun MenuSection(
    viewModel: CashierViewModel, products: List<Product>, formatRp: NumberFormat, isAdmin: Boolean,
    onProductClick: (Product) -> Unit, onEditClick: (Product) -> Unit, onDeleteClick: (Product) -> Unit
) {
    Column {
        OutlinedTextField(
            value = viewModel.searchQuery, onValueChange = { viewModel.searchQuery = it },
            label = { Text("Cari Menu...") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null, tint = ThemeCoffeeMedium) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeCoffeeDark, focusedLabelColor = ThemeCoffeeDark, cursorColor = ThemeCoffeeDark)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.categories) { category ->
                CategoryChip(category, category == viewModel.selectedCategory) { viewModel.selectedCategory = category }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Produk tidak ditemukan.", color = Color.Gray) }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    MenuCard(product, formatRp, isAdmin, { onProductClick(product) }, { onEditClick(product) }, { onDeleteClick(product) })
                }
            }
        }
    }
}

@Composable
fun MenuCard(product: Product, formatRp: NumberFormat, isAdmin: Boolean, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isOutOfStock = product.stock <= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { if (isAdmin) onEdit() else if (!isOutOfStock) onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isOutOfStock) Color(0xFFF5F5F5) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.titleMedium, color = if (isOutOfStock) Color.Gray else ThemeCoffeeDark)
                Text(product.category, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatRp.format(product.price), fontWeight = FontWeight.Bold, color = ThemeCoffeeMedium, fontSize = 14.sp)
                    Text("${product.stock} pcs", fontSize = 12.sp, color = if (isOutOfStock) Color.Red else Color.Gray)
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (!isAdmin) {
                    Button(
                        onClick = onClick, modifier = Modifier.fillMaxWidth().height(40.dp), contentPadding = PaddingValues(0.dp),
                        enabled = !isOutOfStock,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isOutOfStock) Color.LightGray else ThemeCoffeeDark),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text(if (isOutOfStock) "HABIS" else "TAMBAH", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                } else {
                    Surface(color = ThemeCream, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("✎ Ketuk untuk Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ThemeCoffeeMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
            if (isAdmin) {
                IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color(0xFFFFEBEE), CircleShape).size(32.dp)) {
                    Icon(Icons.Default.Delete, "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CartSection(cartItems: List<CartItem>, totalAmount: Double, formatRp: NumberFormat, onRemove: (CartItem) -> Unit, onDecrease: (CartItem) -> Unit, onPay: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)).padding(16.dp)) {
        Text("Keranjang", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = ThemeCoffeeDark)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (cartItems.isEmpty()) item { Text("Keranjang kosong.", color = Color.Gray, modifier = Modifier.padding(top = 16.dp)) }
            items(cartItems) { item -> CartItemRow(item, formatRp, { onRemove(item) }, { onDecrease(item) }) }
        }
        Divider(modifier = Modifier.padding(vertical = 16.dp), color = ThemeCoffeeLight)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Tagihan", fontSize = 16.sp, color = Color.Gray)
            Text(formatRp.format(totalAmount), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = ThemeCoffeeDark)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onPay, modifier = Modifier.fillMaxWidth().height(56.dp), enabled = cartItems.isNotEmpty(), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = ThemeCoffeeDark)) {
            Icon(Icons.Default.ShoppingCart, null); Spacer(modifier = Modifier.width(8.dp)); Text("BAYAR SEKARANG", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, formatRp: NumberFormat, onRemove: () -> Unit, onDecrease: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.Bold, color = ThemeCoffeeDark)
                Text("${item.quantity} x ${formatRp.format(item.product.price)}", fontSize = 12.sp, color = Color.Gray)
            }
            Text(formatRp.format(item.subtotal), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp), color = ThemeCoffeeMedium)
            IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) { Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ThemeCoffeeMedium) }
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected, onClick = onClick, label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ThemeCoffeeDark, selectedLabelColor = Color.White, containerColor = Color.White, labelColor = ThemeCoffeeDark),
        border = FilterChipDefaults.filterChipBorder(borderColor = if (isSelected) ThemeCoffeeDark else ThemeCoffeeLight)
    )
}

@Composable
fun CheckoutDialog(totalAmount: Double, onDismiss: () -> Unit, onConfirm: (Double, Double, String) -> Unit) {
    var selectedMethod by remember { mutableStateOf("Tunai") }
    val paymentMethods = listOf("Tunai", "QRIS", "Debit")
    var cashInput by remember { mutableStateOf("") }
    var change by remember { mutableStateOf(0.0) }
    var isError by remember { mutableStateOf(false) }
    var shortageAmount by remember { mutableStateOf(0.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pembayaran", fontWeight = FontWeight.Bold, color = ThemeCoffeeDark) },
        text = {
            Column {
                Text(
                    text = "Total: ${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ThemeCoffeeDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    paymentMethods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method; isError = false },
                            label = { Text(method) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ThemeCoffeeDark, selectedLabelColor = Color.White)
                        )
                    }
                }
                if (selectedMethod == "Tunai") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { input ->
                            if (input.all { c -> c.isDigit() }) {
                                cashInput = input
                                val cashValue = input.toDoubleOrNull() ?: 0.0
                                if (cashValue < totalAmount) {
                                    isError = true
                                    shortageAmount = totalAmount - cashValue
                                    change = 0.0
                                } else {
                                    isError = false
                                    shortageAmount = 0.0
                                    change = cashValue - totalAmount
                                }
                            }
                        },
                        label = { Text("Uang Diterima") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeCoffeeDark, focusedLabelColor = ThemeCoffeeDark)
                    )
                    if (isError) {
                        Text("Uang kurang ${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(shortageAmount)}!", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                    }
                    Text("Kembalian: ${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(change)}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp), color = ThemeCoffeeDark)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val cash = if (selectedMethod == "Tunai") cashInput.toDoubleOrNull() ?: 0.0 else totalAmount
                if (selectedMethod == "Tunai" && cash < totalAmount) {
                    isError = true
                    shortageAmount = totalAmount - cash
                } else {
                    onConfirm(cash, change, selectedMethod)
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = ThemeCoffeeDark)) { Text("Bayar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

// --- DIALOG SUKSES (UPDATE: Ada Tombol Cetak) ---
@Composable
fun SuccessDialog(
    cartItems: List<CartItem>,
    total: Double,
    cash: Double,
    change: Double,
    method: String,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
        title = { Text("Transaksi Berhasil!", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(total), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = ThemeCoffeeDark)
                if (method == "Tunai") {
                    Text("Kembali: ${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(change)}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
                Text("Metode: $method", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Ringkasan singkat
                Text("${cartItems.size} Item terjual", fontSize = 12.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tombol Cetak/Share (Outline)
                OutlinedButton(
                    onClick = onPrint,
                    border = BorderStroke(1.dp, ThemeCoffeeDark),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeCoffeeDark)
                ) {
                    // PERBAIKAN: Gunakan Icon Share agar tidak error
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Struk")
                }

                // Tombol Transaksi Baru (Fill)
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeCoffeeDark)
                ) {
                    Text("Baru")
                }
            }
        }
    )
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }; var price by remember { mutableStateOf("") }; var stock by remember { mutableStateOf("") }; val categories = listOf("Coffee", "Non-Coffee", "Main Course", "Snack"); var selectedCategory by remember { mutableStateOf(categories[0]) }; var expanded by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tambah Produk Baru", color = ThemeCoffeeDark) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Produk") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeCoffeeDark, focusedLabelColor = ThemeCoffeeDark)); Box(modifier = Modifier.fillMaxWidth()) { OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) { Text(selectedCategory, color = Color.Black); Spacer(modifier = Modifier.weight(1f)); Text("▼", fontSize = 10.sp) }; DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.7f)) { categories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false }) } } }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeCoffeeDark, focusedLabelColor = ThemeCoffeeDark)); OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeCoffeeDark, focusedLabelColor = ThemeCoffeeDark)) } } }, confirmButton = { Button(onClick = { onSave(name, selectedCategory, price, stock) }, modifier = Modifier.padding(end = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = ThemeCoffeeDark)) { Text("Simpan") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } })
}

@Composable
fun EditProductDialog(product: Product, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf(product.name) }; var price by remember { mutableStateOf(product.price.toInt().toString()) }; var stock by remember { mutableStateOf(product.stock.toString()) }; val categories = listOf("Coffee", "Non-Coffee", "Main Course", "Snack"); var selectedCategory by remember { mutableStateOf(product.category) }; var expanded by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit Produk", color = ThemeCoffeeDark) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeCoffeeDark, focusedLabelColor = ThemeCoffeeDark)); Box { OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(selectedCategory) }; DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { categories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; expanded = false }) } } }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeCoffeeDark, focusedLabelColor = ThemeCoffeeDark)); OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeCoffeeDark, focusedLabelColor = ThemeCoffeeDark)) } } }, confirmButton = { Button(onClick = { onSave(name, selectedCategory, price, stock) }, colors = ButtonDefaults.buttonColors(containerColor = ThemeCoffeeDark)) { Text("Update") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } })
}