package com.example.beanflow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beanflow.data.Product
import com.example.beanflow.data.ProductDao
import com.example.beanflow.data.Transaction
import com.example.beanflow.data.TransactionDao
import com.example.beanflow.data.TransactionDetail
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CashierViewModel(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    // --- BAGIAN 1: DATA PRODUK & PENCARIAN ---

    // Tambahkan "All" di urutan pertama
    val categories = listOf("All", "Coffee", "Non-Coffee", "Main Course", "Snack")

    // Default kategori sekarang "All"
    var selectedCategory by mutableStateOf("All")

    // State Pencarian
    var searchQuery by mutableStateOf("")

    // Ambil data mentah dari DB
    private val _allProducts = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // LOGIKA FILTER UTAMA
    val filteredProducts: StateFlow<List<Product>> = combine(
        _allProducts,
        snapshotFlow { searchQuery },
        snapshotFlow { selectedCategory }
    ) { list, query, category ->
        list.filter { product ->
            // 1. Cek Kategori
            // Jika "All", maka dianggap benar (true). Jika tidak, cek kesamaan nama.
            val isCategoryMatch = if (category == "All") true else product.category.equals(category, ignoreCase = true)

            // 2. Cek Pencarian Nama
            val isSearchMatch = if (query.isBlank()) true else product.name.contains(query, ignoreCase = true)

            // Gabungkan kedua syarat
            isCategoryMatch && isSearchMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- BAGIAN 2: KERANJANG BELANJA ---

    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

    var totalAmount: Double by mutableStateOf(0.0)
        private set

    fun addToCart(product: Product): Boolean {
        val existingItem = _cartItems.find { it.product.id == product.id }

        // Cek jumlah yang sudah ada di keranjang
        val currentQtyInCart = existingItem?.quantity ?: 0

        // LOGIKA VALIDASI:
        // Jika (Jumlah di keranjang + 1) lebih besar dari Stok Produk -> TOLAK
        if (currentQtyInCart + 1 > product.stock) {
            return false // Gagal menambahkan karena stok kurang
        }

        if (existingItem != null) {
            val index = _cartItems.indexOf(existingItem)
            _cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            _cartItems.add(CartItem(product, 1))
        }
        calculateTotal()
        return true // Berhasil
    }

    fun decreaseItem(item: CartItem) {
        val index = _cartItems.indexOf(item)
        if (index != -1) {
            if (item.quantity > 1) {
                _cartItems[index] = item.copy(quantity = item.quantity - 1)
            } else {
                _cartItems.removeAt(index)
            }
        }
        calculateTotal()
    }

    fun removeItem(item: CartItem) {
        _cartItems.remove(item)
        calculateTotal()
    }

    private fun calculateTotal() {
        var subtotal = 0.0
        for (item in _cartItems) {
            subtotal += item.subtotal
        }

        totalAmount = subtotal
    }


    // --- BAGIAN 3: CHECKOUT ---

    fun processTransaction(userId: Int, paymentMethod: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_cartItems.isEmpty()) return@launch

            // 1. Simpan Header Transaksi
            val newTransaction = Transaction(
                userId = userId,
                transactionDate = System.currentTimeMillis(),
                totalAmount = totalAmount,
                paymentMethod = paymentMethod
            )
            val transactionId = transactionDao.insertTransaction(newTransaction)

            // 2. Simpan Detail Item & KURANGI STOK
            val details = _cartItems.map { cartItem ->

                // A. Kurangi Stok di Database Produk
                productDao.decreaseStock(cartItem.product.id, cartItem.quantity)

                // B. Siapkan data untuk riwayat transaksi
                TransactionDetail(
                    transactionId = transactionId.toInt(),
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    price = cartItem.product.price,
                    subtotal = cartItem.subtotal
                )
            }
            // Simpan detail ke database
            transactionDao.insertDetails(details)

            // 3. Bersihkan Keranjang & Panggil Callback Sukses
            _cartItems.clear()
            calculateTotal()
            onSuccess()
        }
    }
}