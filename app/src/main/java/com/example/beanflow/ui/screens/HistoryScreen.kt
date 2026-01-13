package com.example.beanflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beanflow.ui.viewmodel.HistoryViewModel
import com.example.beanflow.ui.viewmodel.TransactionHistoryItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onBack: () -> Unit) {
    val historyList by viewModel.historyList.collectAsState()

    // FORMAT CURRENCY
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    // --- LOGIKA GROUPING PER HARI ---
    // Mengelompokkan list berdasarkan Tanggal (String)
    // format: "Sabtu, 12 Oktober 2024"
    val groupedHistory = remember(historyList) {
        historyList
            .sortedByDescending { it.date } // Urutkan dari yang terbaru
            .groupBy { item ->
                val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                sdf.format(Date(item.date))
            }
    }

    // STATE UNTUK POP-UP
    var selectedTransaction by remember { mutableStateOf<TransactionHistoryItem?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4E342E), // Coffee Dark
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF5F5F5))) {

            if (historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat transaksi.", color = Color.Gray)
                }
            } else {
                // --- LIST GROUPING (LazyColumn) ---
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Loop setiap Group (Hari)
                    groupedHistory.forEach { (dateString, transactions) ->

                        // 1. HITUNG TOTAL PER HARI
                        val dailyTotal = transactions.sumOf { it.totalAmount }

                        // 2. HEADER TANGGAL & TOTAL
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateString,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    fontSize = 14.sp
                                )
                                // Tampilan Total Harian
                                Surface(
                                    color = Color(0xFFE0F2F1), // Hijau muda soft
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Total: ${formatRp.format(dailyTotal)}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00695C),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // 3. ITEM TRANSAKSI PADA HARI TERSEBUT
                        items(transactions) { transaction ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTransaction = transaction
                                        showDetailDialog = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        // Tampilkan Jam saja (karena tanggal sudah di header)
                                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        Text(
                                            text = "Jam ${timeFormat.format(Date(transaction.date))}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = transaction.paymentMethod,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = formatRp.format(transaction.totalAmount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DIALOG DETAIL TRANSAKSI (Sama seperti sebelumnya) ---
        if (showDetailDialog && selectedTransaction != null) {
            val trx = selectedTransaction!!

            AlertDialog(
                onDismissRequest = { showDetailDialog = false },
                containerColor = Color.White,
                title = { Text("Rincian Transaksi", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        val fullDateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                        Text("Waktu: ${fullDateFormat.format(Date(trx.date))}", fontSize = 12.sp, color = Color.Gray)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // --- DAFTAR BARANG ---
                        Column(
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            val listBelanja = trx.items
                            for (data in listBelanja) {
                                val itemMap = data as? Map<String, Any>
                                if (itemMap != null) {
                                    val name = itemMap["productName"] as? String ?: "Unknown"
                                    val qty = (itemMap["quantity"] as? Number)?.toInt() ?: 0
                                    val subtotal = (itemMap["subtotal"] as? Number)?.toDouble() ?: 0.0

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$name (x$qty)", fontSize = 14.sp)
                                        Text(formatRp.format(subtotal), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL BAYAR", fontWeight = FontWeight.Bold)
                            Text(formatRp.format(trx.totalAmount), fontWeight = FontWeight.Bold, color = Color(0xFF4E342E))
                        }
                        Text("Metode: ${trx.paymentMethod}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top=4.dp))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showDetailDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E))
                    ) {
                        Text("Tutup")
                    }
                }
            )
        }
    }
}