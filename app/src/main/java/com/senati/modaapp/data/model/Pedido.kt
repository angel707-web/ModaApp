package com.senati.modaapp.data.model

data class Pedido(
    val id: Int = 0,
    val idCliente: Int,
    val fecha: String,
    val total: Double,
    val estado: String = "PENDIENTE",
    val fechaAtencion: String? = null,
    val nombresCliente: String = "",
    val apellidosCliente: String = "",
    val telefonoCliente: String = "",
    val cantidadPrendas: Int = 0
)
