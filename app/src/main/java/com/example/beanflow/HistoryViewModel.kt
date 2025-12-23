package com.example.beanflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beanflow.data.Transaction
import com.example.beanflow.data.TransactionDao
import com.example.beanflow.data.TransactionDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class HistoryViewModel(private val transactionDao: TransactionDao) : ViewModel() {

    // List Transaksi yang ditampilkan
    private val _transactionList = MutableStateFlow<List<Transaction>>(emptyList())
    val transactionList: StateFlow<List<Transaction>> = _transactionList

    // List Detail (untuk dialog)
    private val _selectedTransactionDetails = MutableStateFlow<List<TransactionDetail>>(emptyList())
    val selectedTransactionDetails: StateFlow<List<TransactionDetail>> = _selectedTransactionDetails

    // Status Filter (Teks untuk ditampilkan di UI, misal: "Hari Ini" atau "17 Agu 2024")
    private val _filterStatus = MutableStateFlow("Semua Waktu")
    val filterStatus: StateFlow<String> = _filterStatus

    // Default: Load Semua saat pertama buka
    init {
        loadAllTransactions()
    }

    // 1. Load SEMUA Data
    fun loadAllTransactions() {
        viewModelScope.launch {
            _transactionList.value = transactionDao.getAllTransactions()
            _filterStatus.value = "Semua Riwayat"
        }
    }

    // 2. Load Data berdasarkan TANGGAL TERTENTU (Timestamp milidetik)
    fun loadTransactionsByDate(dateMillis: Long) {
        viewModelScope.launch {
            // Hitung jam 00:00:00 s/d 23:59:59 dari tanggal yang dipilih
            val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val start = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val end = calendar.timeInMillis

            // Ambil data dari database
            _transactionList.value = transactionDao.getTransactionsByDate(start, end)

            // Update status teks
            val dateString = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID")).format(Date(dateMillis))
            _filterStatus.value = dateString
        }
    }

    // Load Detail Barang
    fun loadDetails(transactionId: Int) {
        viewModelScope.launch {
            _selectedTransactionDetails.value = transactionDao.getTransactionDetails(transactionId)
        }
    }

    fun clearDetails() {
        _selectedTransactionDetails.value = emptyList()
    }
}