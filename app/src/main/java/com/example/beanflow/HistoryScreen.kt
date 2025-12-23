package com.example.beanflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange // Icon Kalender
import androidx.compose.material.icons.filled.Refresh // Icon Reset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beanflow.data.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit
) {
    val transactions by viewModel.transactionList.collectAsState()
    val details by viewModel.selectedTransactionDetails.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf(0) }

    // Formatter
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    // STATE DATE PICKER
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Riwayat Transaksi", fontSize = 18.sp)
                        Text(filterStatus, fontSize = 12.sp, color = Color.Gray) // Menampilkan status filter
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Tombol Reset (Tampilkan Semua)
                    IconButton(onClick = { viewModel.loadAllTransactions() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Filter")
                    }
                    // Tombol Pilih Tanggal
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                    }
                }
            )
        }
    ) { paddingValues ->

        // KONTEN UTAMA
        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Tidak ada transaksi pada periode ini.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                items(transactions) { trx ->
                    TransactionCard(trx, formatRp, dateFormat) {
                        selectedId = trx.id
                        viewModel.loadDetails(trx.id)
                        showDetailDialog = true
                    }
                }
            }
        }

        // --- 1. DIALOG DATE PICKER (PILIH TANGGAL) ---
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        // Ambil tanggal yang dipilih user (timestamp)
                        val selectedDate = datePickerState.selectedDateMillis
                        if (selectedDate != null) {
                            viewModel.loadTransactionsByDate(selectedDate)
                        }
                        showDatePicker = false
                    }) {
                        Text("Pilih")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // --- 2. DIALOG DETAIL STRUK ---
        if (showDetailDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDetailDialog = false
                    viewModel.clearDetails()
                },
                title = { Text("Detail Struk #$selectedId") },
                text = {
                    if (details.isEmpty()) {
                        Text("Memuat data...")
                    } else {
                        Column {
                            Row(Modifier.fillMaxWidth()) {
                                Text("Item", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                Text("Qty", Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                                Text("Total", fontWeight = FontWeight.Bold)
                            }
                            Divider(Modifier.padding(vertical = 4.dp))
                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                items(details) { item ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                        Text(item.productName, Modifier.weight(1f), fontSize = 14.sp)
                                        Text("x${item.quantity}", Modifier.width(40.dp), fontSize = 14.sp)
                                        Text(formatRp.format(item.subtotal), fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDetailDialog = false
                        viewModel.clearDetails()
                    }) {
                        Text("Tutup")
                    }
                }
            )
        }
    }
}

// Komponen Kartu (Sama seperti sebelumnya)
@Composable
fun TransactionCard(
    transaction: Transaction,
    formatRp: NumberFormat,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "ID Struk: #${transaction.id}", fontWeight = FontWeight.Bold)
                Text(text = dateFormat.format(Date(transaction.transactionDate)), fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total Bayar:", fontSize = 14.sp)
                Text(text = formatRp.format(transaction.totalAmount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}