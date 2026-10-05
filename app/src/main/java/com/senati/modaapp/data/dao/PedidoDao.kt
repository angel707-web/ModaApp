package com.senati.modaapp.data.dao

import android.content.ContentValues
import android.content.Context
import com.senati.modaapp.data.DBHelper
import com.senati.modaapp.data.model.ItemCarrito
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PedidoDao(context: Context) {

    private val dbHelper = DBHelper(context)

    /**
     * Registra un pedido completo y sus detalles en una sola transacción atómica (HU-09 CA3)
     */
    fun registrar(idCliente: Int, items: List<ItemCarrito>): Long {
        if (items.isEmpty()) return -1L

        val db = dbHelper.writableDatabase
        var idPedidoGenerado = -1L
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val total = items.sumOf { it.subtotal }

        db.beginTransaction()
        try {
            // 1. Insertar el encabezado del pedido
            val pedidoValues = ContentValues().apply {
                put(DBHelper.COL_PEDIDO_ID_CLIENTE, idCliente)
                put(DBHelper.COL_PEDIDO_FECHA, fechaActual)
                put(DBHelper.COL_PEDIDO_TOTAL, total)
                put(DBHelper.COL_PEDIDO_ESTADO, "PENDIENTE")
            }
            idPedidoGenerado = db.insert(DBHelper.TABLE_PEDIDO, null, pedidoValues)

            if (idPedidoGenerado > 0) {
                // 2. Insertar cada detalle del pedido y actualizar stock
                for (item in items) {
                    val detalleValues = ContentValues().apply {
                        put(DBHelper.COL_DETALLE_ID_PEDIDO, idPedidoGenerado)
                        put(DBHelper.COL_DETALLE_ID_ROPA, item.ropa.id)
                        put(DBHelper.COL_DETALLE_CANTIDAD, item.cantidad)
                        put(DBHelper.COL_DETALLE_PRECIO_UNIT, item.ropa.precio)
                        put(DBHelper.COL_DETALLE_SUBTOTAL, item.subtotal)
                    }
                    db.insert(DBHelper.TABLE_DETALLE_PEDIDO, null, detalleValues)

                    // Descontar inventario de la prenda
                    db.execSQL(
                        "UPDATE ${DBHelper.TABLE_ROPA} SET ${DBHelper.COL_ROPA_CANTIDAD} = ${DBHelper.COL_ROPA_CANTIDAD} - ? WHERE ${DBHelper.COL_ROPA_ID} = ?",
                        arrayOf(item.cantidad, item.ropa.id)
                    )
                }

                db.setTransactionSuccessful()
            }
        } finally {
            db.endTransaction()
        }

        return idPedidoGenerado
    }
}
