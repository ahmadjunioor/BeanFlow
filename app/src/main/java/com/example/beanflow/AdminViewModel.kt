package com.example.beanflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beanflow.data.Product
import com.example.beanflow.data.ProductDao
import com.example.beanflow.data.TransactionDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

// PERHATIKAN: Konstruktor sekarang menerima 'transactionDao'
class AdminViewModel(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    // 1. Data Produk
    val productList: StateFlow<List<Product>> = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Data Omzet Hari Ini (INI YANG DIBUTUHKAN ADMIN SCREEN)
    private val todayRange: Pair<Long, Long> get() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    val todaySales: StateFlow<Double> = transactionDao.getTotalSales(todayRange.first, todayRange.second)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val topProducts: StateFlow<List<com.example.beanflow.data.TopProduct>> = transactionDao.getTopSellingProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Fungsi CRUD
    fun addProduct(name: String, price: String, stock: String, category: String) {
        viewModelScope.launch {
            val priceDouble = price.toDoubleOrNull() ?: 0.0
            val stockInt = stock.toIntOrNull() ?: 0
            val newProduct = Product(name = name, price = priceDouble, stock = stockInt, category = category, imagePath = null)
            productDao.insertProduct(newProduct)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productDao.softDeleteProduct(product.id)
        }
    }

    fun updateProduct(originalProduct: Product, name: String, price: String, stock: String, category: String) {
        viewModelScope.launch {
            val updatedProduct = originalProduct.copy(
                name = name,
                price = price.toDoubleOrNull() ?: 0.0,
                stock = stock.toIntOrNull() ?: 0,
                category = category
            )
            productDao.updateProduct(updatedProduct)
        }
    }
}