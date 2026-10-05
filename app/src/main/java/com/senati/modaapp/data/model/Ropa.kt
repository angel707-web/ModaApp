package com.senati.modaapp.data.model

data class Ropa(
    val id: Int = 0,
    val modelo: String,
    val idCategoria: Int,
    val nombreCategoria: String = "",
    val talla: String,
    val marca: String,
    val color: String,
    val precio: Double,
    val cantidad: Int,
    val foto: String? = null
)
