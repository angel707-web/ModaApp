package com.senati.modaapp.data.model

data class ItemCarrito(
    val ropa: Ropa,
    var cantidad: Int
) {
    val subtotal: Double
        get() = ropa.precio * cantidad
}
