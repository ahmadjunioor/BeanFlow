package com.example.beanflow.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beanflow.data.local.ProductDao
import com.example.beanflow.data.local.TransactionDao
import com.example.beanflow.data.model.CartItem
import com.example.beanflow.data.model.Product
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CashierViewModel(
    private val productDao: ProductDao, // Disimpan agar MainActivity tidak error
    private val transactionDao: TransactionDao
) : ViewModel() {

    // --- SETUP DATABASE FIREBASE ---
    private val db = Firebase.firestore

    // --- BAGIAN 1: DATA PRODUK & PENCARIAN ---
    val categories = listOf("All", "Coffee", "Non-Coffee", "Main Course", "Snack")
    var selectedCategory by mutableStateOf("All")
    var searchQuery by mutableStateOf("")

    // Data Produk Live dari Firebase
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    init {
        fetchProductsFromFirebase()
    }

    // Ambil data Realtime
    private fun fetchProductsFromFirebase() {
        db.collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error mengambil data: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val productList = snapshot.documents.mapNotNull { doc ->
                        val product = doc.toObject(Product::class.java)
                        product?.firestoreId = doc.id
                        product
                    }
                    _allProducts.value = productList
                }
            }
    }

    // Logic Filter (Pencarian & Kategori)
    val filteredProducts: StateFlow<List<Product>> = combine(
        _allProducts,
        snapshotFlow { searchQuery },
        snapshotFlow { selectedCategory }
    ) { list, query, category ->
        list.filter { product ->
            val isCategoryMatch = if (category == "All") true else product.category.equals(category, ignoreCase = true)
            val isSearchMatch = if (query.isBlank()) true else product.name.contains(query, ignoreCase = true)
            isCategoryMatch && isSearchMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- BAGIAN 2: KERANJANG BELANJA ---
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

    var totalAmount: Double by mutableStateOf(0.0)
        private set

    fun addToCart(product: Product): Boolean {
        // Cek keranjang pakai ID Firebase
        val existingItem = _cartItems.find { it.product.firestoreId == product.firestoreId }
        val currentQtyInCart = existingItem?.quantity ?: 0

        if (currentQtyInCart + 1 > product.stock) {
            return false
        }

        if (existingItem != null) {
            val index = _cartItems.indexOf(existingItem)
            _cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            _cartItems.add(CartItem(product, 1))
        }
        calculateTotal()
        return true
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
        totalAmount = _cartItems.sumOf { it.subtotal }
    }

    // --- BAGIAN 3: TRANSAKSI (FULL FIREBASE) ---
    fun processTransaction(userId: Int, paymentMethod: String, onSuccess: () -> Unit) {
        if (_cartItems.isEmpty()) return

        val transactionData = hashMapOf(
            "userId" to userId,
            "date" to System.currentTimeMillis(),
            "totalAmount" to totalAmount,
            "paymentMethod" to paymentMethod,
            "items" to _cartItems.map { item ->
                mapOf(
                    "productName" to item.product.name,
                    "price" to item.product.price,
                    "quantity" to item.quantity,
                    "subtotal" to item.subtotal,
                    "productId" to item.product.firestoreId
                )
            }
        )

        db.collection("transactions")
            .add(transactionData)
            .addOnSuccessListener {
                updateStockInFirebase()
                _cartItems.clear()
                calculateTotal()
                onSuccess()
            }
            .addOnFailureListener { e ->
                println("Gagal menyimpan transaksi: ${e.message}")
            }
    }

    private fun updateStockInFirebase() {
        _cartItems.forEach { item ->
            if (item.product.firestoreId.isNotEmpty()) {
                val productRef = db.collection("products").document(item.product.firestoreId)
                productRef.update("stock", FieldValue.increment(-item.quantity.toLong()))
            }
        }
    }

    // --- BAGIAN 4: ADMIN (FUNGSI YANG HILANG TADI) ---
    fun addProductToFirebase(name: String, category: String, price: String, stock: String) {
        val priceDouble = price.toDoubleOrNull() ?: 0.0
        val stockInt = stock.toIntOrNull() ?: 0

        if (name.isNotEmpty() && priceDouble > 0) {
            val newProduct = hashMapOf(
                "name" to name,
                "category" to category,
                "price" to priceDouble,
                "stock" to stockInt,
                "firestoreId" to "" // Nanti diisi otomatis
            )

            db.collection("products").add(newProduct)
        }
    }
    // --- FITUR UPDATE & DELETE PRODUK ---

    // 1. Fungsi Hapus Produk
    fun deleteProduct(product: Product) {
        if (product.firestoreId.isNotEmpty()) {
            db.collection("products").document(product.firestoreId)
                .delete()
                .addOnSuccessListener { println("Produk berhasil dihapus") }
                .addOnFailureListener { println("Gagal hapus: ${it.message}") }
        }
    }

    // 2. Fungsi Update/Edit Produk
    fun updateProduct(product: Product, newName: String, newCategory: String, newPrice: String, newStock: String) {
        if (product.firestoreId.isNotEmpty()) {
            val priceDouble = newPrice.toDoubleOrNull() ?: product.price
            val stockInt = newStock.toIntOrNull() ?: product.stock

            val updates = hashMapOf<String, Any>(
                "name" to newName,
                "category" to newCategory,
                "price" to priceDouble,
                "stock" to stockInt
            )

            db.collection("products").document(product.firestoreId)
                .update(updates)
                .addOnSuccessListener { println("Produk berhasil diupdate") }
        }
    }
}