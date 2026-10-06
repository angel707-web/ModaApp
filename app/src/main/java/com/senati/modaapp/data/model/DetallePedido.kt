package com.senati.modaapp.data.model

data class DetallePedido(
    val id: Int = 0,
    val idPedido: Int,
    val idRopa: Int,
    val cantidad: Int,
    val precioUnit: Double,
    val subtotal: Double,
    val modeloRopa: String = "",
    val tallaRopa: String = "",
    val colorRopa: String = "",
    val fotoRopa: String = ""
)
