package com.senati.modaapp.data.dao

import android.content.ContentValues
import android.content.Context
import com.senati.modaapp.data.DBHelper
import com.senati.modaapp.data.model.DetallePedido
import com.senati.modaapp.data.model.ItemCarrito
import com.senati.modaapp.data.model.Pedido
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ResultadoAtencion {
    object Exito : ResultadoAtencion()
    data class Error(val mensaje: String) : ResultadoAtencion()
}

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
                // 2. Insertar cada detalle del pedido
                for (item in items) {
                    val detalleValues = ContentValues().apply {
                        put(DBHelper.COL_DETALLE_ID_PEDIDO, idPedidoGenerado)
                        put(DBHelper.COL_DETALLE_ID_ROPA, item.ropa.id)
                        put(DBHelper.COL_DETALLE_CANTIDAD, item.cantidad)
                        put(DBHelper.COL_DETALLE_PRECIO_UNIT, item.ropa.precio)
                        put(DBHelper.COL_DETALLE_SUBTOTAL, item.subtotal)
                    }
                    db.insert(DBHelper.TABLE_DETALLE_PEDIDO, null, detalleValues)
                }

                db.setTransactionSuccessful()
            }
        } finally {
            db.endTransaction()
        }

        return idPedidoGenerado
    }

    /**
     * Lista pedidos por estado ('PENDIENTE' o 'ATENDIDO') ordenados del más reciente al más antiguo (HU-11 CA1)
     */
    fun listarPorEstado(estado: String): List<Pedido> {
        val lista = mutableListOf<Pedido>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT p.${DBHelper.COL_PEDIDO_ID},
                   p.${DBHelper.COL_PEDIDO_ID_CLIENTE},
                   p.${DBHelper.COL_PEDIDO_FECHA},
                   p.${DBHelper.COL_PEDIDO_TOTAL},
                   p.${DBHelper.COL_PEDIDO_ESTADO},
                   p.${DBHelper.COL_PEDIDO_FECHA_ATENCION},
                   c.${DBHelper.COL_CLIENTE_NOMBRES},
                   c.${DBHelper.COL_CLIENTE_APELLIDOS},
                   c.${DBHelper.COL_CLIENTE_TELEFONO},
                   COALESCE((SELECT SUM(d.${DBHelper.COL_DETALLE_CANTIDAD}) 
                             FROM ${DBHelper.TABLE_DETALLE_PEDIDO} d 
                             WHERE d.${DBHelper.COL_DETALLE_ID_PEDIDO} = p.${DBHelper.COL_PEDIDO_ID}), 0) AS total_prendas
            FROM ${DBHelper.TABLE_PEDIDO} p
            INNER JOIN ${DBHelper.TABLE_CLIENTE} c ON p.${DBHelper.COL_PEDIDO_ID_CLIENTE} = c.${DBHelper.COL_CLIENTE_ID}
            WHERE p.${DBHelper.COL_PEDIDO_ESTADO} = ?
            ORDER BY p.${DBHelper.COL_PEDIDO_ID} DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(estado))
        if (cursor.moveToFirst()) {
            do {
                val pedido = Pedido(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ID)),
                    idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ID_CLIENTE)),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_FECHA)) ?: "",
                    total = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_TOTAL)),
                    estado = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ESTADO)) ?: "PENDIENTE",
                    fechaAtencion = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_FECHA_ATENCION)),
                    nombresCliente = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_NOMBRES)) ?: "",
                    apellidosCliente = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_APELLIDOS)) ?: "",
                    telefonoCliente = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_TELEFONO)) ?: "",
                    cantidadPrendas = cursor.getInt(cursor.getColumnIndexOrThrow("total_prendas"))
                )
                lista.add(pedido)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    /**
     * Obtiene los detalles de un pedido con modelo, talla, color y foto de la prenda (HU-11 CA2)
     */
    fun obtenerDetalles(idPedido: Int): List<DetallePedido> {
        val lista = mutableListOf<DetallePedido>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT d.${DBHelper.COL_DETALLE_ID},
                   d.${DBHelper.COL_DETALLE_ID_PEDIDO},
                   d.${DBHelper.COL_DETALLE_ID_ROPA},
                   d.${DBHelper.COL_DETALLE_CANTIDAD},
                   d.${DBHelper.COL_DETALLE_PRECIO_UNIT},
                   d.${DBHelper.COL_DETALLE_SUBTOTAL},
                   r.${DBHelper.COL_ROPA_MODELO},
                   r.${DBHelper.COL_ROPA_TALLA},
                   r.${DBHelper.COL_ROPA_COLOR},
                   r.${DBHelper.COL_ROPA_FOTO}
            FROM ${DBHelper.TABLE_DETALLE_PEDIDO} d
            INNER JOIN ${DBHelper.TABLE_ROPA} r ON d.${DBHelper.COL_DETALLE_ID_ROPA} = r.${DBHelper.COL_ROPA_ID}
            WHERE d.${DBHelper.COL_DETALLE_ID_PEDIDO} = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idPedido.toString()))
        if (cursor.moveToFirst()) {
            do {
                val detalle = DetallePedido(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_ID)),
                    idPedido = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_ID_PEDIDO)),
                    idRopa = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_ID_ROPA)),
                    cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_CANTIDAD)),
                    precioUnit = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_PRECIO_UNIT)),
                    subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_DETALLE_SUBTOTAL)),
                    modeloRopa = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_MODELO)) ?: "",
                    tallaRopa = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_TALLA)) ?: "",
                    colorRopa = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_COLOR)) ?: "",
                    fotoRopa = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_FOTO)) ?: ""
                )
                lista.add(detalle)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    /**
     * Obtiene un pedido por su ID con datos del cliente
     */
    fun obtenerPorId(idPedido: Int): Pedido? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT p.${DBHelper.COL_PEDIDO_ID},
                   p.${DBHelper.COL_PEDIDO_ID_CLIENTE},
                   p.${DBHelper.COL_PEDIDO_FECHA},
                   p.${DBHelper.COL_PEDIDO_TOTAL},
                   p.${DBHelper.COL_PEDIDO_ESTADO},
                   p.${DBHelper.COL_PEDIDO_FECHA_ATENCION},
                   c.${DBHelper.COL_CLIENTE_NOMBRES},
                   c.${DBHelper.COL_CLIENTE_APELLIDOS},
                   c.${DBHelper.COL_CLIENTE_TELEFONO},
                   COALESCE((SELECT SUM(d.${DBHelper.COL_DETALLE_CANTIDAD}) 
                             FROM ${DBHelper.TABLE_DETALLE_PEDIDO} d 
                             WHERE d.${DBHelper.COL_DETALLE_ID_PEDIDO} = p.${DBHelper.COL_PEDIDO_ID}), 0) AS total_prendas
            FROM ${DBHelper.TABLE_PEDIDO} p
            INNER JOIN ${DBHelper.TABLE_CLIENTE} c ON p.${DBHelper.COL_PEDIDO_ID_CLIENTE} = c.${DBHelper.COL_CLIENTE_ID}
            WHERE p.${DBHelper.COL_PEDIDO_ID} = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idPedido.toString()))
        var pedido: Pedido? = null
        if (cursor.moveToFirst()) {
            pedido = Pedido(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ID)),
                idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ID_CLIENTE)),
                fecha = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_FECHA)) ?: "",
                total = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_TOTAL)),
                estado = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ESTADO)) ?: "PENDIENTE",
                fechaAtencion = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_FECHA_ATENCION)),
                nombresCliente = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_NOMBRES)) ?: "",
                apellidosCliente = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_APELLIDOS)) ?: "",
                telefonoCliente = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_TELEFONO)) ?: "",
                cantidadPrendas = cursor.getInt(cursor.getColumnIndexOrThrow("total_prendas"))
            )
        }
        cursor.close()
        return pedido
    }

    /**
     * Marca un pedido como atendido, valida stock y descuenta inventario en una transacción atómica (HU-11 CA3, CA4)
     */
    fun atender(idPedido: Int): ResultadoAtencion {
        val db = dbHelper.writableDatabase
        val fechaAtencion = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        db.beginTransaction()
        try {
            // 1. Obtener detalles del pedido
            val detalles = mutableListOf<Triple<Int, Int, String>>() // idRopa, cantidad, modelo
            val queryDetalles = """
                SELECT d.${DBHelper.COL_DETALLE_ID_ROPA},
                       d.${DBHelper.COL_DETALLE_CANTIDAD},
                       r.${DBHelper.COL_ROPA_MODELO},
                       r.${DBHelper.COL_ROPA_CANTIDAD}
                FROM ${DBHelper.TABLE_DETALLE_PEDIDO} d
                INNER JOIN ${DBHelper.TABLE_ROPA} r ON d.${DBHelper.COL_DETALLE_ID_ROPA} = r.${DBHelper.COL_ROPA_ID}
                WHERE d.${DBHelper.COL_DETALLE_ID_PEDIDO} = ?
            """.trimIndent()

            val cursor = db.rawQuery(queryDetalles, arrayOf(idPedido.toString()))
            if (cursor.moveToFirst()) {
                do {
                    val idRopa = cursor.getInt(0)
                    val cantSolicitada = cursor.getInt(1)
                    val modelo = cursor.getString(2) ?: ""
                    val stockActual = cursor.getInt(3)

                    // CA4: Si una prenda no tiene stock suficiente, abortar
                    if (stockActual < cantSolicitada) {
                        cursor.close()
                        return ResultadoAtencion.Error(modelo)
                    }
                    detalles.add(Triple(idRopa, cantSolicitada, modelo))
                } while (cursor.moveToNext())
            }
            cursor.close()

            // 2. Descontar inventario de cada prenda
            for (item in detalles) {
                db.execSQL(
                    "UPDATE ${DBHelper.TABLE_ROPA} SET ${DBHelper.COL_ROPA_CANTIDAD} = ${DBHelper.COL_ROPA_CANTIDAD} - ? WHERE ${DBHelper.COL_ROPA_ID} = ?",
                    arrayOf(item.second, item.first)
                )
            }

            // 3. Actualizar estado y fecha de atención del pedido
            val updateValues = ContentValues().apply {
                put(DBHelper.COL_PEDIDO_ESTADO, "ATENDIDO")
                put(DBHelper.COL_PEDIDO_FECHA_ATENCION, fechaAtencion)
            }
            db.update(
                DBHelper.TABLE_PEDIDO,
                updateValues,
                "${DBHelper.COL_PEDIDO_ID} = ?",
                arrayOf(idPedido.toString())
            )

            db.setTransactionSuccessful()
            return ResultadoAtencion.Exito
        } catch (e: Exception) {
            return ResultadoAtencion.Error(e.message ?: "Error al atender el pedido")
        } finally {
            db.endTransaction()
        }
    }
}
