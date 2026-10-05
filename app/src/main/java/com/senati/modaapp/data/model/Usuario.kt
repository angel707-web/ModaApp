package com.senati.modaapp.data.model

data class Usuario(
    val id: Int = 0,
    val usuario: String,
    val clave: String,
    val rol: String = "ADMIN",
    val telefono: String? = null
)
