package com.example.beanflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data Class dengan field 'items'
data class TransactionHistoryItem(
    val id: String,
    val date: Long,
    val totalAmount: Double,
    val paymentMethod: String,
    val items: List<Map<String, Any>> // Field ini wajib diisi
)

class HistoryViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _historyList = MutableStateFlow<List<TransactionHistoryItem>>(emptyList())
    val historyList: StateFlow<List<TransactionHistoryItem>> = _historyList

    init {
        fetchHistoryFromFirebase()
    }

    private fun fetchHistoryFromFirebase() {
        db.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error mengambil history: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->

                        // 1. AMBIL DATA ITEMS DARI FIREBASE (Agar tidak error)
                        // Kita ambil sebagai List of Map, kalau kosong kasih list kosong
                        val itemsData = doc.get("items") as? List<Map<String, Any>> ?: emptyList()

                        TransactionHistoryItem(
                            id = doc.id,
                            date = doc.getLong("date") ?: 0L,
                            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                            paymentMethod = doc.getString("paymentMethod") ?: "Tunai",
                            items = itemsData // 2. MASUKKAN DATA ITEMS KE SINI
                        )
                    }
                    _historyList.value = list
                }
            }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }
}