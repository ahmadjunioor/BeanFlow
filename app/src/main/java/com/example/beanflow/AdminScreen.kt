package com.example.beanflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beanflow.data.Product
import com.example.beanflow.data.TopProduct
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AdminViewModel, onLogout: () -> Unit) {
    val products by viewModel.productList.collectAsState()
    val todaySales by viewModel.todaySales.collectAsState()
    // DATA BARU: TOP PRODUCT
    val topProducts by viewModel.topProducts.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Admin") },
                actions = { TextButton(onClick = onLogout) { Text("Logout", color = Color.Red) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                productToEdit = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { paddingValues ->

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            // BAGIAN 1: KARTU OMZET
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Pendapatan Hari Ini", fontSize = 14.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRp.format(todaySales),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // BAGIAN 2: TOP 5 PRODUK TERLARIS (BARU)
            if (topProducts.isNotEmpty()) {
                item {
                    Text("🔥 5 Produk Terlaris", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)) // Warna Oranye Muda
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            topProducts.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${index + 1}. ${item.name}", fontWeight = FontWeight.SemiBold)
                                    Text("${item.totalQty} terjual", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                                }
                                if (index < topProducts.size - 1) Divider(color = Color.White)
                            }
                        }
                    }
                }
            }

            // BAGIAN 3: DAFTAR MANAJEMEN MENU
            item {
                Text("Manajemen Menu", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            if (products.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada produk.")
                    }
                }
            } else {
                items(products) { product ->
                    ProductItem(
                        product = product,
                        formatRp = formatRp,
                        onEdit = { productToEdit = product; showDialog = true },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }
        }

        if (showDialog) {
            ProductFormDialog(
                productToEdit = productToEdit,
                onDismiss = { showDialog = false },
                onConfirm = { name, price, stock, cat ->
                    if (productToEdit == null) {
                        viewModel.addProduct(name, price, stock, cat)
                    } else {
                        viewModel.updateProduct(productToEdit!!, name, price, stock, cat)
                    }
                    showDialog = false
                }
            )
        }
    }
}

// --- KOMPONEN LAIN TETAP SAMA ---

@Composable
fun ProductItem(product: Product, formatRp: NumberFormat, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Stok: ${product.stock} | ${product.category}", fontSize = 14.sp, color = Color.DarkGray)
                Text(text = formatRp.format(product.price), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red) }
        }
    }
}

@Composable
fun ProductFormDialog(productToEdit: Product?, onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var price by remember { mutableStateOf(productToEdit?.price?.toInt()?.toString() ?: "") }
    var stock by remember { mutableStateOf(productToEdit?.stock?.toString() ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "Coffee") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Coffee", "Non-Coffee", "Main Course", "Snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (productToEdit == null) "Tambah Menu Baru" else "Edit Menu") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Menu") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { if (it.all { char -> char.isDigit() }) price = it }, label = { Text("Harga (Rp)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = stock, onValueChange = { if (it.all { char -> char.isDigit() }) stock = it }, label = { Text("Stok") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = category, onValueChange = {}, label = { Text("Kategori") }, readOnly = true, trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown") }, modifier = Modifier.fillMaxWidth())
                    Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { selection -> DropdownMenuItem(text = { Text(selection) }, onClick = { category = selection; expanded = false }) }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(name, price, stock, category) }, enabled = name.isNotEmpty() && price.isNotEmpty()) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}