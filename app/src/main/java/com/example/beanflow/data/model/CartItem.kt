package com.example.beanflow.data.model

// Helper class untuk menampung item di keranjang
data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    // Hitung subtotal otomatis (Harga x Jumlah)
    val subtotal: Double
        get() = product.price * quantity
}