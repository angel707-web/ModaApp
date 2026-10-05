package com.senati.modaapp.data

import com.senati.modaapp.data.model.ItemCarrito
import com.senati.modaapp.data.model.Ropa

object Carrito {

    private val items = mutableListOf<ItemCarrito>()

    fun obtenerItems(): List<ItemCarrito> = items.toList()

    fun agregar(ropa: Ropa, cantidad: Int): Boolean {
        if (cantidad <= 0) return false

        val itemExistente = items.find { it.ropa.id == ropa.id }
        if (itemExistente != null) {
            val nuevaCantidad = itemExistente.cantidad + cantidad
            if (nuevaCantidad > ropa.cantidad) {
                // No puede superar el stock disponible
                itemExistente.cantidad = ropa.cantidad
            } else {
                itemExistente.cantidad = nuevaCantidad
            }
        } else {
            val cantidadFinal = if (cantidad > ropa.cantidad) ropa.cantidad else cantidad
            items.add(ItemCarrito(ropa, cantidadFinal))
        }
        return true
    }

    fun quitar(item: ItemCarrito): Boolean {
        return items.remove(item)
    }

    fun vaciar() {
        items.clear()
    }

    fun obtenerTotal(): Double {
        return items.sumOf { it.subtotal }
    }

    fun obtenerCantidadTotal(): Int {
        return items.sumOf { it.cantidad }
    }

    fun estaVacio(): Boolean = items.isEmpty()
}
