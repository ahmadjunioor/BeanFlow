package com.example.beanflow.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class AdminViewModel : ViewModel() {
    private val db = Firebase.firestore

    // --- STATE PENDAPATAN ---
    var totalRevenue by mutableStateOf(0.0)
    var dailyRevenue by mutableStateOf(0.0)
    var monthlyRevenue by mutableStateOf(0.0)

    // --- STATE TRANSAKSI & STOK ---
    var totalTransactionCount by mutableStateOf(0) // Total Semua (Opsional, jika butuh)
    var dailyTransactionCount by mutableStateOf(0) // KHUSUS HARI INI (Fitur Baru)

    var lowStockCount by mutableStateOf(0)

    // --- DATA UNTUK POP-UP ---
    var lowStockList by mutableStateOf<List<Map<String, Any>>>(emptyList())
    var recentTransactions by mutableStateOf<List<Map<String, Any>>>(emptyList())

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        // 1. Ambil Semua Transaksi
        db.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    var tempTotalRev = 0.0
                    var tempDailyRev = 0.0
                    var tempMonthlyRev = 0.0
                    var tempDailyTransCount = 0 // Penghitung Transaksi Hari Ini

                    // --- SIAPKAN PENANDA WAKTU ---
                    val calendar = Calendar.getInstance()

                    // A. Waktu Mulai Hari Ini (00:00:00)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startOfDay = calendar.timeInMillis

                    // B. Waktu Mulai Bulan Ini (Tanggal 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val startOfMonth = calendar.timeInMillis

                    // --- LOOPING DATA ---
                    for (doc in snapshot.documents) {
                        val amount = doc.getDouble("totalAmount") ?: 0.0
                        val date = doc.getLong("date") ?: 0L

                        // 1. Total Seumur Hidup
                        tempTotalRev += amount

                        // 2. Cek HARI INI
                        if (date >= startOfDay) {
                            tempDailyRev += amount
                            tempDailyTransCount++ // Tambah hitungan transaksi hari ini
                        }

                        // 3. Cek BULAN INI
                        if (date >= startOfMonth) {
                            tempMonthlyRev += amount
                        }
                    }

                    // Update State UI
                    totalRevenue = tempTotalRev
                    dailyRevenue = tempDailyRev
                    monthlyRevenue = tempMonthlyRev

                    totalTransactionCount = snapshot.size() // Total Keseluruhan
                    dailyTransactionCount = tempDailyTransCount // Total Hari Ini (Yang akan ditampilkan)

                    // Ambil 10 Transaksi Terakhir
                    recentTransactions = snapshot.documents.take(10).map { doc ->
                        mapOf(
                            "date" to (doc.getLong("date") ?: 0L),
                            "amount" to (doc.getDouble("totalAmount") ?: 0.0),
                            "method" to (doc.getString("paymentMethod") ?: "-")
                        )
                    }
                }
            }

        // 2. Hitung Stok Menipis
        db.collection("products")
            .whereLessThan("stock", 5)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    lowStockCount = snapshot.size()
                    lowStockList = snapshot.documents.map { doc ->
                        mapOf(
                            "name" to (doc.getString("name") ?: "Unknown"),
                            "stock" to (doc.getLong("stock") ?: 0L)
                        )
                    }
                }
            }
    }
}